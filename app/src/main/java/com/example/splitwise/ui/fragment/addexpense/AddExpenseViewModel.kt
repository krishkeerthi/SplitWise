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

    var memberIds = mutableSetOf<Int>()

    var payerId: Int? = null

    var category: Int? = null

    init {
        getGroupMembers()
    }

    fun createExpense(name: String, category: Int, payer: Int, amount: Float, ids: List<Int>){
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

            for(id in ids)
                expenseRepository.addExpensePayee(expenseId, id)

            groupRepository.addGroupExpense(groupId, expenseId)
        }
    }

    private fun getGroupMembers() {
        viewModelScope.launch {
            val ids = groupRepository.getGroupMembers(groupId)
            _members.value = getMembersFromIds(ids)

            if(ids != null)
                memberIds = ids.toMutableSet()
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