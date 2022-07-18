package com.example.splitwise.ui.fragment.expenses

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.model.ExpenseMember
import com.example.splitwise.ui.fragment.addexpense.AddExpenseViewModel
import kotlinx.coroutines.launch
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

    init {
        viewModelScope.launch {
            val expenses = expenseRepository.getExpenses(groupId)

            if (expenses != null) {
                val expenseMembers = mutableListOf<ExpenseMember>()

                for(expense in expenses){
                    val member = memberRepository.getMember(expense.payer)

                    if(member != null)
                    expenseMembers.add(toExpenseMember(expense, member))
                }

                _expenseMembers.value = expenseMembers
            }

            val memberIds = groupRepository.getGroupMembers(groupId)
            _groupMembers.value = getMembersFromIds(memberIds)
        }
    }

    private fun getMembersFromIds(memberIds: List<Int>?): List<Member>? {
        return if(memberIds != null){
            val members = mutableListOf<Member>()

            for(id in memberIds)
                viewModelScope.launch {
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

    private fun toExpenseMember(expense: Expense, member: Member): ExpenseMember{
        return ExpenseMember(
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

class ExpensesViewModelFactory(private val context: Context, private val groupId: Int):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpensesViewModel(context, groupId) as T
    }
}