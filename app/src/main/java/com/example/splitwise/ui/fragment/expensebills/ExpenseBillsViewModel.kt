package com.example.splitwise.ui.fragment.expensebills

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseBillsViewModel(context: Context, private val expenseId: Int): ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val expenseRepository = ExpenseRepository(database)

    private val _bills = MutableLiveData<List<Uri>?>()

    val bills: LiveData<List<Uri>?>
        get() = _bills

//    init {
//        viewModelScope.launch {
//            fetchBills()
//        }
//    }

    fun fetchBills() {
        viewModelScope.launch {
            _bills.value = expenseRepository.getExpenseBills(expenseId)
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

    fun addBills(uri: Uri) {
        viewModelScope.launch {
            expenseRepository.addExpenseBill(expenseId, uri)
            fetchBills()
        }
    }

}

class ExpenseBillsViewModelFactory(private val context: Context, private val expenseId: Int) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpenseBillsViewModel(context, expenseId) as T
    }
}