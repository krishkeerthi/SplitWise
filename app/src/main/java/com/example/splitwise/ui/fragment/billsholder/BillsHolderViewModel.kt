package com.example.splitwise.ui.fragment.billsholder

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.model.BillUri
import kotlinx.coroutines.launch

class BillsHolderViewModel(context: Context, private val expenseId: Int, var position: Int) :
    ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val expenseRepository = ExpenseRepository(database)

    private val _bills = MutableLiveData<List<BillUri>?>() // Previous List<Uri>?

    val bills: LiveData<List<BillUri>?>
        get() = _bills

    init {
        viewModelScope.launch {
            fetchBills()
        }

    }

    private suspend fun fetchBills() {
        //_bills.value = expenseRepository.getExpenseBills(expenseId)
        _bills.value = expenseRepository.getExpenseBillsWithId(expenseId)
    }

    fun getBills(): List<String> {
        val billsString = mutableListOf<String>()

        bills.value?.let { bills ->
            for (bill in bills)
                billsString.add(bill.uri.toString())
        }

        return billsString
    }

    fun deleteBill(currentPosition: Int) {

        viewModelScope.launch {
            bills.value?.let { bills ->
                if (currentPosition < bills.size) {

                    val billId = bills[currentPosition].id
                    expenseRepository.deleteBill(billId)

                    position = if((currentPosition - 1) < 0) 0 else {currentPosition - 1}

                    fetchBills()
                }
            }
        }
    }

}

class BillsHolderViewModelFactory(
    private val context: Context,
    private val expenseId: Int,
    private val position: Int
) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BillsHolderViewModel(context, expenseId, position) as T
    }
}