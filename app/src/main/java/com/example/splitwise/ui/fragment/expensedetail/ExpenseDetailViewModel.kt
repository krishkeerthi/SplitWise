package com.example.splitwise.ui.fragment.expensedetail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.MemberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpenseDetailViewModel(context: Context, private val expenseId: Int) : ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val expenseRepository = ExpenseRepository(database)
    private val memberRepository = MemberRepository(database)

    private val _expense = MutableLiveData<Expense?>()

    val expense: LiveData<Expense?>
        get() = _expense

    private val _payer = MutableLiveData<Member?>()

    val payer: LiveData<Member?>
        get() = _payer

    private val _payees = MutableLiveData<List<Member>?>()

    val payees: LiveData<List<Member>?>
        get() = _payees

    private val _bills = MutableLiveData<List<Uri>?>()

    val bills: LiveData<List<Uri>?>
        get() = _bills

    init {
        viewModelScope.launch {

            val expense = expenseRepository.getExpense(expenseId)
            _expense.value = expense

            if (expense != null) {
                val member = memberRepository.getMember(expense.payer)
                _payer.value = member
            }

            val memberIds: List<Int>? = expenseRepository.getExpensePayees(expenseId)
            _payees.value = getMembersFromIds(memberIds)

            fetchBills()

        }
    }

    private suspend fun fetchBills() {
        _bills.value = expenseRepository.getExpenseBills(expenseId)
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

    fun addBills(uri: Uri) {
        viewModelScope.launch {
            expenseRepository.addExpenseBill(expenseId, uri)
            fetchBills()
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
}


class ExpenseDetailViewModelFactory(private val context: Context, private val expenseId: Int) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpenseDetailViewModel(context, expenseId) as T
    }
}