package com.example.splitwise.ui.fragment.expensesoverview

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.ui.fragment.addexpense.AddExpenseViewModel
import kotlinx.coroutines.launch

class ExpensesOverviewViewModel(context: Context, private val groupId: Int): ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val expenseRepository = ExpenseRepository(database)

    private val _expenses = MutableLiveData<List<Expense>?>()

    val expenses: LiveData<List<Expense>?>
        get() = _expenses


    init {
        viewModelScope.launch {
            _expenses.value = expenseRepository.getExpenses(groupId)
        }
    }
}

class ExpensesOverviewViewModelFactory(private val context: Context, private val groupId: Int):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpensesOverviewViewModel(context, groupId) as T
    }
}