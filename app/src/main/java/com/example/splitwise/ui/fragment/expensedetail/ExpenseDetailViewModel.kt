package com.example.splitwise.ui.fragment.expensedetail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.ui.fragment.addexpense.AddExpenseViewModel
import kotlinx.coroutines.launch

class ExpenseDetailViewModel(context: Context, private val expenseId: Int): ViewModel(){

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

            if(expense != null){
                val member = memberRepository.getMember(expense.payer)
                _payer.value = member
            }

            val memberIds: List<Int>? = expenseRepository.getExpensePayees(expenseId)
            _payees.value = getMembersFromIds(memberIds)

            _bills.value = expenseRepository.getExpenseBills(expenseId)

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

class ExpenseDetailViewModelFactory(private val context: Context, private val expenseId: Int):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpenseDetailViewModel(context, expenseId) as T
    }
}