package com.example.splitwise.ui.fragment.expensedetail

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

class ExpenseDetailViewModel(context: Context, private val expenseId: Int) : ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val expenseRepository = ExpenseRepository(database)
    private val memberRepository = MemberRepository(database)
    private val groupRepository = GroupRepository(database)
    private val transactionRepository = TransactionRepository(database)

    private val _expense = MutableLiveData<Expense?>()

    val expense: LiveData<Expense?>
        get() = _expense

    private val _payer = MutableLiveData<Member?>()

    val payer: LiveData<Member?>
        get() = _payer

    private val _payees = MutableLiveData<List<Member>?>()

    val payees: LiveData<List<Member>?>
        get() = _payees

    private val _removedPayees = MutableLiveData<List<Member>?>()

    val removedPayees: LiveData<List<Member>?>
        get() = _removedPayees

    private val _bills = MutableLiveData<List<Uri>?>()

    val bills: LiveData<List<Uri>?>
        get() = _bills

    init {
        viewModelScope.launch {

            fetchMembers()

            //fetchBills()

        }
    }

    fun fetchMembers(){
        viewModelScope.launch {
            val expense = expenseRepository.getExpense(expenseId)
            _expense.value = expense

            if (expense != null) {
                val member = memberRepository.getMember(expense.payer)
                _payer.value = member
            }

            val memberIds: List<Int>? = expenseRepository.getExpensePayees(expenseId)
            _payees.value = getMembersFromIds(memberIds)

            fetchRemovedMembers()
        }
    }

    private suspend fun fetchRemovedMembers(){
        val removedPayeesIds: List<Int>? = expenseRepository.getRemovedExpensePayees(expenseId)
        _removedPayees.value = getMembersFromIds(removedPayeesIds)
    }

    fun fetchBills() {
        viewModelScope.launch {
            _bills.value = expenseRepository.getExpenseBills(expenseId)
        }

    }

    private suspend fun getMembersFromIds(memberIds: List<Int>?): MutableList<Member>? {
        return withContext(Dispatchers.IO) {
            if (memberIds != null) {
                val members = mutableListOf<Member>()

                for (id in memberIds) {
                    val member = memberRepository.getMember(id)
                    member?.let {
                        members.add(it)
                    }
                }

                members
            } else
                null
        }
    }

    fun addBills(uri: Uri) {
        viewModelScope.launch {
            expenseRepository.addExpenseBill(expenseId, uri)
            fetchBills()
        }
    }

    fun getBills(): List<String> {
        val billsString = mutableListOf<String>()

        bills.value?.let { uriList ->
            for (uri in uriList)
                billsString.add(uri.toString())
        }

        return billsString
    }

    fun deletePayee(expenseId: Int, member: Member, onDelete: () -> Unit, noDeletion: () -> Unit){
        viewModelScope.launch {

            expenseRepository.getExpense(expenseId)?.let {
                if(it.payer == member.memberId){
                    noDeletion()
                }
                else{
                    // 1. reduce streak
                    memberRepository.decrementStreak(member.memberId)
                    // 2. delete record in expense payee
                    expenseRepository.removeExpensePayee(expenseId, member.memberId)
                    expenseRepository.addPayeeToRemovedExpensePayees(expenseId, member.memberId)
                    // 3. reduce expense's split amount in transaction
                    expenseRepository.getExpense(expenseId)?.let { expense ->
                        transactionRepository.getAmount(expense.groupId, expense.payer, member.memberId)?.let { amount ->
                            val updatedAmount = amount - expense.splitAmount
                            if(updatedAmount >= 0) {
                                transactionRepository.updateAmount(
                                    expense.groupId,
                                    expense.payer,
                                    member.memberId,
                                    updatedAmount
                                )
                            }
                            else{ // later ref transaction
                                transactionRepository.updateAmount(
                                    expense.groupId,
                                    expense.payer,
                                    member.memberId,
                                    0f
                                )
                                transactionRepository.updateAmount(
                                    expense.groupId,
                                    member.memberId,
                                    expense.payer,
                                    updatedAmount.absoluteValue
                                )
                            }

                            onDelete()
                        }
                    }
                }
            }

        }
    }

    fun deleteExpense(groupId: Int, expenseId: Int, onDelete: () -> Unit){
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

                    //4. reduce amount in transaction
                    for(payeeId in payeesIds){
                        transactionRepository.getAmount(groupId, expense.payer, payeeId)?.let { amount ->
                            val updatedAmount = amount - expense.splitAmount
                            if(updatedAmount >= 0) {
                                transactionRepository.updateAmount(groupId, expense.payer, payeeId, updatedAmount)
                            }
                            else{
                                transactionRepository.updateAmount(groupId, expense.payer, payeeId, 0f)
                                transactionRepository.updateAmount(groupId, payeeId, expense.payer, updatedAmount.absoluteValue)
                            }
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

                onDelete()
            }

        }
    }

}


class ExpenseDetailViewModelFactory(private val context: Context, private val expenseId: Int) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpenseDetailViewModel(context, expenseId) as T
    }
}