package com.example.splitwise.ui.fragment.groupsoverview

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.util.Category
import com.example.splitwise.util.getCategory
import com.example.splitwise.util.titleCase
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import kotlin.math.exp

class GroupsOverviewViewModel(context: Context) : ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupRepository = GroupRepository(database)
    private val expenseRepository = ExpenseRepository(database)

    private val _groups = MutableLiveData<List<Group>?>()

    val groups: LiveData<List<Group>?>
        get() = _groups


    private val categoryAmount = mutableMapOf<Category, Float>()

    private val _pieEntries = MutableLiveData<List<PieEntry>>()

    val pieEntries: LiveData<List<PieEntry>>
        get() = _pieEntries

    init {
        //getGroups()
//        categoryAmount[Category.TICKETS] = 0F
//        categoryAmount[Category.FEES] = 0F
//        categoryAmount[Category.FOOD] = 0F
//        categoryAmount[Category.REPAIRS] = 0F
//        categoryAmount[Category.RENT] = 0F
//        categoryAmount[Category.TRAVEL] = 0F
//        categoryAmount[Category.ENTERTAINMENT] = 0F
//        categoryAmount[Category.ESSENTIALS] = 0F
//        categoryAmount[Category.OTHERS] = 0F
//
//        getChartData()
    }

    private fun getGroups() {
        viewModelScope.launch {
            _groups.value = groupRepository.getGroups()
        }
    }

    private fun getChartData() {
        viewModelScope.launch {
            val groups = groupRepository.getGroups()

            groups?.let { allGroups ->
                for (group in allGroups) {
                    val expenses = expenseRepository.getExpenses(group.groupId)

                    expenses?.let { allExpenses ->
                        for (expense in allExpenses) {
                            Log.d(TAG, "getChartData: expense: ${expense.totalAmount}")
                            Log.d(TAG, "getChartData: category: ${getCategory(expense.category).name}")
                            val category = getCategory(expense.category)

                            val currentAmount = categoryAmount[category] ?: 0f
                            categoryAmount[category] = currentAmount + expense.totalAmount
//                            if (categoryAmount[category] == null){
//                                categoryAmount[category] = 0F
//                                Log.d(TAG, "getChartData: category is null")
//                            }
//                            else {
//
//                                categoryAmount[category]?.plus(expense.totalAmount)
//                                Log.d(TAG, "getChartData: category not null ${categoryAmount[category]}")
//                            }

                        }
                    }
                }
            }

            val entries= categoryAmount.entries

            val pieEntries = mutableListOf<PieEntry>()

            for (i in entries) {
                if(i.value != 0f) {
                    pieEntries.add(PieEntry(i.value, i.key.name.titleCase()))
                    Log.d(
                        TAG,
                        "getChartData: category = ${i.key.name.titleCase()} amount = ${i.value}"
                    )
                }
            }

            _pieEntries.value = pieEntries
        }

    }
}


class GroupsOverviewViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupsOverviewViewModel(context) as T
    }
}