package com.example.splitwise.ui.fragment.addexpense

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
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

class AddExpenseViewModel(context: Context, private val groupId: Int): ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)
    private val groupRepository = GroupRepository(database)
    private val expenseRepository = ExpenseRepository(database)
    private val transactionRepository = TransactionRepository(database)

    private val _members = MutableLiveData<List<Member>?>()

    val members: LiveData<List<Member>?>
        get() = _members

    var memberIds = mutableSetOf<Int>()

    var payer: Member? = null

    var category: Category? = null

    init {
        getGroupMembers()
    }

    fun createExpense(name: String, category: Int, payer: Int, amount: Float, memberIds: List<Int>,
    gotoExpenseFragment: () -> Unit){
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

            Log.d(TAG, "createExpense: payer: $payer, \n members: ${memberIds}")

            for(id in memberIds) {
                // add expense payee
                expenseRepository.addExpensePayee(expenseId, id)


                // increment payee streak // later ref
                // memberRepository.incrementStreak(id)

                memberRepository.createOrIncrementStreak(id)

                if(id != payer) {
                    //transactionRepository.addTransaction(groupId, payer, id, splitAmount)
                    transactionRepository.updateTransaction(groupId, payer, id, splitAmount)
                }
            }

            groupRepository.addGroupExpense(groupId, expenseId)

            groupRepository.updateTotalExpense(groupId, amount)


            gotoExpenseFragment()
        }
    }

    private fun getGroupMembers() {
        viewModelScope.launch {
            val ids = groupRepository.getGroupMembers(groupId)

            if(ids != null)
                memberIds = ids.toMutableSet()

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

}

class AddExpenseViewModelFactory(private val context: Context, private val groupId: Int):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddExpenseViewModel(context, groupId) as T
    }
}