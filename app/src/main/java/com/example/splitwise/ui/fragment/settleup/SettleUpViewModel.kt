package com.example.splitwise.ui.fragment.settleup

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import com.example.splitwise.ui.fragment.addexpense.AddExpenseViewModel
import kotlinx.coroutines.launch

class SettleUpViewModel(context: Context,
                        val payerId: Int,
                        val groupId: Int?): ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val transactionRepository = TransactionRepository(database)
    private val memberRepository = MemberRepository(database)
    private val groupRepository = GroupRepository(database)

    var payeeId: Int? = null

    private val _group = MutableLiveData<Group?>()
    val group: LiveData<Group?>
        get() = _group

    private val _payer = MutableLiveData<Member?>()
    val payer: LiveData<Member?>
        get() = _payer

    private val _payees = MutableLiveData<List<Member>?>()
    val payees: LiveData<List<Member>?>
        get() = _payees

    private val _amount = MutableLiveData<Float?>()
    val amount: LiveData<Float?>
        get() = _amount

    init {
        viewModelScope.launch {
            val member = memberRepository.getMember(payerId)
            _payer.value = member

            val memberIds: List<Int>?= if(groupId != null){
                transactionRepository.getPayers(payerId, groupId)
            }
            else{
            transactionRepository.getPayers(payerId)
            }

            _payees.value = getMembersFromIds(memberIds)

            if(groupId != null){
                _group.value = groupRepository.getGroup(groupId)
            }
        }
    }

    fun getAmount(senderId: Int, receiverId: Int? = null, groupId: Int? = null){
        viewModelScope.launch {
            if(groupId != null){
                if(receiverId != null){
                    _amount.value = transactionRepository.getOwedInGroup(senderId, receiverId, groupId)
                }
                else{
                    _amount.value = transactionRepository.getOwedInGroup(senderId, groupId)
                }
            }
            else{
                if(receiverId != null){
                    _amount.value = transactionRepository.getOwed(senderId, receiverId)
                }
                else{
                    _amount.value = transactionRepository.getOwed(senderId)
                }
            }
        }
    }

    fun settle(senderId: Int, receiverId: Int? = null, groupId: Int? = null){
        viewModelScope.launch {
            if(groupId != null){
                if(receiverId != null){
                    transactionRepository.settle(senderId, receiverId, groupId)
                }
                else{
                    transactionRepository.settleAllInGroup(senderId, groupId)
                }
            }
            else{
                if(receiverId != null){
                    transactionRepository.settle(senderId, receiverId)
                }
                else{
                    transactionRepository.settleAll(senderId)
                }
            }
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

class SettleUpViewModelFactory(private val context: Context, private val payerId: Int, private val groupId: Int?):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettleUpViewModel(context, payerId, groupId) as T
    }
}