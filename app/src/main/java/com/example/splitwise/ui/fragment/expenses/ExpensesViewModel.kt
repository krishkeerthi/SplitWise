package com.example.splitwise.ui.fragment.expenses

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.model.ExpenseMember
import com.example.splitwise.ui.fragment.addexpense.AddExpenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.exp

class ExpensesViewModel(context: Context, val groupId: Int): ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupRepository = GroupRepository(database)
    private val memberRepository = MemberRepository(database)
    private val expenseRepository = ExpenseRepository(database)

    private val _groupMembers = MutableLiveData<List<Member>?>()

    val groupMembers: LiveData<List<Member>?>
        get() = _groupMembers

    private val _expenseMembers = MutableLiveData<List<ExpenseMember>?>()

    val expenseMembers: LiveData<List<ExpenseMember>?>
        get() = _expenseMembers

    fun fetchData() {
        Log.d(TAG, "expenseviewmodel: created")
        viewModelScope.launch {
            val expenses = expenseRepository.getExpenses(groupId)

            if (expenses != null) {
                Log.d(TAG, "viewmodel: group expenses $expenses")
                val expenseMembers = mutableListOf<ExpenseMember>()

                for(expense in expenses){
                    val member = memberRepository.getMember(expense.payer)

                    if(member != null)
                    expenseMembers.add(toExpenseMember(expense, member))
                }

                _expenseMembers.value = expenseMembers
            }

            val memberIds = groupRepository.getGroupMembers(groupId)
            Log.d(TAG, "viewmodel: group members $memberIds with groupid ${groupId}")
            _groupMembers.value = getMembersFromIds(memberIds)
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

    private suspend fun toExpenseMember(expense: Expense, member: Member): ExpenseMember{
        return withContext(Dispatchers.IO) {
            ExpenseMember(
                expense.expenseId,
                expense.groupId,
                expense.expenseName,
                expense.category,
                expense.totalAmount,
                expense.splitAmount,
                expense.payer,
                member,
                expense.date
            )
        }
    }

}

class ExpensesViewModelFactory(private val context: Context, private val groupId: Int):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpensesViewModel(context, groupId) as T
    }
}