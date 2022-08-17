package com.example.splitwise.ui.fragment.expenses

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.model.ExpenseMember
import com.example.splitwise.util.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpensesViewModel(context: Context, val groupId: Int) : ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupRepository = GroupRepository(database)
    private val memberRepository = MemberRepository(database)
    private val expenseRepository = ExpenseRepository(database)

    private val _group = MutableLiveData<Group?>()

    val group: LiveData<Group?>
        get() = _group

    private val _groupMembers = MutableLiveData<List<Member>?>()

    var pending: Boolean = false
    var pendingCategory: Category? = null

    private val _running = MutableLiveData<Boolean>()
    val running: LiveData<Boolean>
        get() = _running

    val groupMembers: LiveData<List<Member>?>
        get() = _groupMembers

    private val _expenseMembers = MutableLiveData<List<ExpenseMember>?>()

    val expenseMembers: LiveData<List<ExpenseMember>?>
        get() = _expenseMembers

    private val _expenseCount = MutableLiveData<Int>(0)

    val expenseCount: LiveData<Int>
        get() = _expenseCount

    val checkedFilters = mutableListOf<Category>()

    private val _checkedFiltersCount = MutableLiveData<Int>(0)

    val checkedFiltersCount: LiveData<Int>
        get() = _checkedFiltersCount

    init {
        fetchData()
        _running.value = false
    }

    fun fetchData() {
        Log.d(TAG, "expenseviewmodel: created")
        viewModelScope.launch {
            _group.value = groupRepository.getGroup(groupId)

            setExpenseMembers(true)

            val memberIds = groupRepository.getGroupMembers(groupId)
            Log.d(TAG, "viewmodel: group members $memberIds with groupid $groupId")
            _groupMembers.value = getMembersFromIds(memberIds)

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

    private suspend fun toExpenseMember(expense: Expense, member: Member): ExpenseMember {
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

    private suspend fun setExpenseMembers(initialLoad: Boolean = false) {

        withContext(Dispatchers.Main) {


            val ordinals = mutableListOf<Int>()
            for (category in checkedFilters) {
                ordinals.add(category.ordinal)
            }

//            val expenses = category?.let { expenseRepository.getExpensesByCategory(groupId, it.ordinal) }
//                ?: expenseRepository.getExpenses(groupId)

            val expenses = if (initialLoad || ordinals.isEmpty())
                expenseRepository.getExpenses(groupId)
            else
                expenseRepository.getExpensesByCategories(groupId, ordinals)

            if (expenses != null) {
                // setting expense count on initial load
                    if(initialLoad)
                        _expenseCount.value = expenses.size

                Log.d(TAG, "viewmodel: group expenses $expenses")
                val expenseMembers = mutableListOf<ExpenseMember>()

                for (expense in expenses) {
                    val member = memberRepository.getMember(expense.payer)

                    if (member != null)
                        expenseMembers.add(toExpenseMember(expense, member))
                }

                _expenseMembers.value = expenseMembers
            }
        }
    }

    fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
        }
    }

    fun filterByCategory() {
        if (_running.value == false)
            viewModelScope.launch {
                _running.value = true
                setExpenseMembers()
                _running.value = false

            }
        else {
            pending = true
            pendingCategory = Category.OTHERS // need to work
        }

    }

    fun decrementCheckedFiltersCount() {
        _checkedFiltersCount.value = _checkedFiltersCount.value?.minus(1)
    }

    fun incrementCheckedFiltersCount() {
        _checkedFiltersCount.value = _checkedFiltersCount.value?.plus(1)
    }

//    fun clearCategoryFilter() {
//        viewModelScope.launch {
//            _pending.value = false
//            Log.d(TAG, "clearCategoryFilter: started")
//            setExpenseMembers()
//            Log.d(TAG, "clearCategoryFilter: ended")
//            _pending.value = true
//        }
//    }

}

class ExpensesViewModelFactory(private val context: Context, private val groupId: Int) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpensesViewModel(context, groupId) as T
    }
}