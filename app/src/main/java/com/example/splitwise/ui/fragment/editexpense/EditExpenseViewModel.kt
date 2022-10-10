package com.example.splitwise.ui.fragment.editexpense

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import com.example.splitwise.util.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class EditExpenseViewModel(context: Context, private val groupId: Int, private val expense: Expense) :ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)
    private val groupRepository = GroupRepository(database)
    private val expenseRepository = ExpenseRepository(database)
    private val transactionRepository = TransactionRepository(database)

    private val _members = MutableLiveData<List<Member>?>()

    val members: LiveData<List<Member>?>
        get() = _members

    var memberIds = mutableSetOf<Int>()

    var membersUpdated = false

    var payer: Member? = null

    var category: Category? = null

    init {
        getGroupMembers()
    }

    fun updateExpense(name: String, category: Int, payer: Int, amount: Float, memberIds: List<Int>,
                      gotoExpenseDetailFragment: (Int) -> Unit){
        viewModelScope.launch {
            deleteExpense(groupId, expense.expenseId)

            createExpense(name, category, payer, amount, memberIds, gotoExpenseDetailFragment)
        }
    }

    private fun createExpense(name: String, category: Int, payer: Int, amount: Float, memberIds: List<Int>,
                      gotoExpenseDetailFragment: (Int) -> Unit){
        viewModelScope.launch {
            val splitAmount = amount / (memberIds.size)
            val expenseId = expenseRepository.createExpense(
                groupId,
                name,
                category,
                amount,
                splitAmount,
                payer,
                Date()
            )

            // where payer streak incremented
            // No need because he may not be in that transaction, if he/she is then membersId will have it

            Log.d(ContentValues.TAG, "createExpense: payer: $payer, \n members: $memberIds")

            for(id in memberIds) {
                // add expense payee
                expenseRepository.addExpensePayee(expenseId, id)
                // increment payee streak
                memberRepository.incrementStreak(id)

                if(id != payer) {
                    //transactionRepository.addTransaction(groupId, payer, id, splitAmount)
                    transactionRepository.updateTransaction(groupId, payer, id, splitAmount)
                }
            }

            groupRepository.addGroupExpense(groupId, expenseId)

            groupRepository.updateTotalExpense(groupId, amount)


            gotoExpenseDetailFragment(expenseId)
        }
    }

    private suspend fun deleteExpense(groupId: Int, expenseId: Int){
        viewModelScope.launch {
            //1. delete bills
            expenseRepository.deleteBills(expenseId)
            //2. delete group expense
            expenseRepository.removeExpenseIdFromGroup(groupId, expenseId)

            expenseRepository.getExpense(expenseId)?.let { expense ->
                // 3. decrement member streak
                memberRepository.decrementStreak(expense.payer)

                expenseRepository.getExpensePayees(expenseId)?.let { payeesIds ->
                    // decrementing streak
                    for(payeeId in payeesIds)
                        memberRepository.decrementStreak(payeeId)

                    // ok
                    //4. reduce amount in transaction
                    for(payeeId in payeesIds){
                        transactionRepository.getAmount(groupId, expense.payer, payeeId)?.let { amount ->
                            transactionRepository.updateAmount(groupId, expense.payer, payeeId, amount - expense.splitAmount)
                        }
                    }

                    //5. delete expense payees
                    for(payeeId in payeesIds)
                        expenseRepository.removeExpensePayee(expenseId, payeeId)

                }

                //6. reduce expense total in groups
                groupRepository.getTotalExpense(groupId)?.let { total ->
                    Log.d(ContentValues.TAG, "deleteExpense: group total ${total} expense total ${expense.totalAmount}")
                    groupRepository.reduceTotalExpense(groupId, total - expense.totalAmount)
                }
                //7. delete expense in expenses
                expenseRepository.deleteExpense(expenseId)
            }
        }.join()
    }

    private fun getGroupMembers() {
        viewModelScope.launch {
            val ids = groupRepository.getGroupMembers(groupId)

            // This is set so that by default all payees will be selected.
//            if(ids != null)
//                memberIds = ids.toMutableSet()

            _members.value = getMembersFromIds(ids)


        }
    }

    private suspend fun getMembersFromIds(memberIds: List<Int>?): MutableList<Member>? {
        return withContext(Dispatchers.IO){
            if(memberIds != null){
                val members = mutableListOf<Member>()

                for(id in memberIds) {
                    val member = memberRepository.getMember(id)
                    member?.let {
                        members.add(it)
                    }
                }

                members
            }
            else
                null
        }
    }

    fun getPayer(payerId: Int, members: List<Member>): Member? {
        for(member in members){
            if(member.memberId == payerId)
                return member
        }
        return null
    }
}

class EditExpenseViewModelFactory(private val context: Context, private val groupId: Int, private val expense: Expense):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditExpenseViewModel(context, groupId, expense) as T
    }
}