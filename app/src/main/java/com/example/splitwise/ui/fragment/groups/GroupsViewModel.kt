package com.example.splitwise.ui.fragment.groups

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import com.example.splitwise.model.AmountFilterModel
import com.example.splitwise.model.DateFilterModel
import com.example.splitwise.model.FilterModel
import com.example.splitwise.util.AmountFilter
import com.example.splitwise.util.DateFilter
import com.example.splitwise.util.GroupFilter
import kotlinx.coroutines.launch
import java.util.*

class GroupsViewModel(context: Context) : ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupsRepository = GroupRepository(database)
    private val memberRepository = MemberRepository(database)
    private val expenseRepository = ExpenseRepository(database)
    private val transactionRepository = TransactionRepository(database)

    private val _groups = MutableLiveData<List<Group>?>()

    val groups: LiveData<List<Group>?>
        get() = _groups

    // Filter related properties
    var remainingFilters = GroupFilter.values().toMutableList()

    val filterModel = FilterModel(null, null)

    var selectedDateFilter: DateFilter = DateFilter.BEFORE
    var selectedAmountFilter: AmountFilter = AmountFilter.BELOW

    var bool = true
//    init {
//        filterModel.amountFilterModel = AmountFilterModel(AmountFilter.ABOVE, 89f)
//    }

//    init{
//        Log.d(TAG, "groupsviewmodel: called")
//        viewModelScope.launch {
//            _groups.value = groupsRepository.getGroups()
//        }
//    }

//    init {
//        //fetchData()
//    }

    fun fetchData() {
        Log.d(TAG, "groupsviewmodel: called")
        viewModelScope.launch {
            //_groups.value = groupsRepository.getGroups()

            applyFilter()
        }
    }


    fun applyDateFilter(date: Date) {
        val dateFilter = DateFilterModel(selectedDateFilter, date)
        filterModel.dateFilterModel = dateFilter

        remainingFilters.remove(GroupFilter.DATE)

        applyFilter()
    }

    fun applyAmountFilter(amount: Float) {
        val amountFilter = AmountFilterModel(selectedAmountFilter, amount)
        filterModel.amountFilterModel = amountFilter

        remainingFilters.remove(GroupFilter.AMOUNT)

        applyFilter()
    }

    private fun applyFilter() {
        val dateFilterModel = filterModel.dateFilterModel
        val date = dateFilterModel?.date
        val amountFilterModel = filterModel.amountFilterModel
        val amount = amountFilterModel?.amount

        viewModelScope.launch {
            if (dateFilterModel != null && amountFilterModel != null) {
                _groups.value =
                    if (dateFilterModel.dateFilter == DateFilter.BEFORE && amountFilterModel.amountFilter == AmountFilter.BELOW) {
                        groupsRepository.getGroupsCreatedBeforeAndAmountBelow(date!!, amount!!)
                    } else if (dateFilterModel.dateFilter == DateFilter.BEFORE && amountFilterModel.amountFilter == AmountFilter.ABOVE) {
                        groupsRepository.getGroupsCreatedBeforeAndAmountAbove(date!!, amount!!)
                    } else if (dateFilterModel.dateFilter == DateFilter.AFTER && amountFilterModel.amountFilter == AmountFilter.ABOVE) {
                        groupsRepository.getGroupsCreatedAfterAndAmountAbove(date!!, amount!!)
                    } else {
                        groupsRepository.getGroupsCreatedAfterAndAmountBelow(date!!, amount!!)
                    }
            } else if (dateFilterModel != null && amountFilterModel == null) {
                _groups.value = if (dateFilterModel.dateFilter == DateFilter.AFTER) {
                    groupsRepository.getGroupsCreatedAfter(date!!)
                } else {
                    groupsRepository.getGroupsCreatedBefore(date!!)
                }
            } else if ((dateFilterModel == null) && (amountFilterModel != null)) {
                _groups.value = if (amountFilterModel.amountFilter == AmountFilter.ABOVE) {
                    groupsRepository.getGroupsWithAmountAbove(amount!!)
                } else {
                    groupsRepository.getGroupsWithAmountBelow(amount!!)
                }

            } else {
                _groups.value = groupsRepository.getGroups()
            }
        }
    }

    fun removeDateFilter() {
        filterModel.dateFilterModel = null
        remainingFilters.add(GroupFilter.DATE)

        applyFilter()
    }

    fun removeAmountFilter() {
        filterModel.amountFilterModel = null
        remainingFilters.add(GroupFilter.AMOUNT)

        applyFilter()
    }

    fun resetFilters() {
        filterModel.dateFilterModel = null
        filterModel.amountFilterModel = null
        remainingFilters = GroupFilter.values().toMutableList()
        applyFilter()
    }

    fun deleteGroup(groupId: Int, onDelete: () -> Unit) {
        viewModelScope.launch {
            expenseRepository.getExpenses(groupId)?.let { expenses ->

                for (expense in expenses) {
                    //1. remove bills
                    expenseRepository.deleteBills(expense.expenseId)

                    // 2. decrement member streak payer
                    //memberRepository.decrementStreak(expense.payer) //later ref

                    expenseRepository.getExpensePayees(expense.expenseId)?.let { payeesIds ->
                        // decrementing streak payees
                        for (payeeId in payeesIds)
                            memberRepository.decrementStreak(payeeId)

                        //3. delete expense payees
                        for(payeeId in payeesIds)
                            expenseRepository.removeExpensePayee(expense.expenseId, payeeId)
                    }

                    //4. delete group expense
                    expenseRepository.removeExpenseIdFromGroup(groupId, expense.expenseId)

                    //5. delete expense
                    expenseRepository.deleteExpense(expense.expenseId)
                }

                //6. transactions
                transactionRepository.deleteGroupTransactions(groupId)

                // 7. remove group members
                groupsRepository.removeGroupMembers(groupId)

                //8. remove group
                groupsRepository.removeGroup(groupId)

                onDelete()
            }
        }
    }
}

class GroupsViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupsViewModel(context) as T
    }
}
