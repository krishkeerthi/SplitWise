package com.example.splitwise.ui.fragment.addexpense

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.*

class AddExpenseViewModel(context: Context, private val groupId: Int): ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)
    private val groupRepository = GroupRepository(database)
    private val expenseRepository = ExpenseRepository(database)

    private val _members = MutableLiveData<List<Member>?>()

    val members: LiveData<List<Member>?>
        get() = _members

    init {
        getGroupMembers()
    }

    fun createExpense(name: String, category: Int, payer: Int, amount: Float, memberIds: List<Int>){
        viewModelScope.launch {
            val expenseId = expenseRepository.createExpense(
                groupId,
                name,
                category,
                amount,
                amount/(memberIds.size),
                payer,
                Date()
            )

            for(memberId in memberIds)
                expenseRepository.addExpensePayee(expenseId, memberId)

            groupRepository.addGroupExpense(groupId, expenseId)
        }
    }

    private fun getGroupMembers() {
        viewModelScope.launch {
            val memberIds = groupRepository.getGroupMembers(groupId)
            _members.value = getMembersFromIds(memberIds)
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

}

class AddExpenseViewModelFactory(private val context: Context, private val groupId: Int):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddExpenseViewModel(context, groupId) as T
    }
}