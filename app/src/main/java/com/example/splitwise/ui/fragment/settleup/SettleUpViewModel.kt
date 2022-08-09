package com.example.splitwise.ui.fragment.settleup

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettleUpViewModel(
    context: Context,
    val payerId: Int,
    val groupIds: List<Int>
) : ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val transactionRepository = TransactionRepository(database)
    private val memberRepository = MemberRepository(database)
    private val groupRepository = GroupRepository(database)

    var payeeId: Int? = null

    private val _groups = MutableLiveData<List<Group>?>()
    val groups: LiveData<List<Group>?>
        get() = _groups

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
        getGroups()
    }

    fun fetchData(){
        viewModelScope.launch {
            val member = memberRepository.getMember(payerId)
            _payer.value = member

            val memberIds: List<Int>? = if (groupIds.isEmpty()) {
                transactionRepository.getPayers(payerId)
            } else {
                transactionRepository.getPayers(payerId, groupIds)
            }

            _payees.value = getMembersFromIds(memberIds)
        }
    }

    private fun getGroups(){
        viewModelScope.launch {
            if (groupIds.isNotEmpty()) {
                _groups.value = groupRepository.getGroups(groupIds)
            }
            else{
                _groups.value = groupRepository.getGroups()
            }
        }
    }

    fun groupIds(): List<Int>{
        val groupIds = mutableListOf<Int>()
        if(groups.value != null){
            for(group in groups.value!!)
                groupIds.add(group.groupId)
        }

        return groupIds.toList()
    }

    fun getAmount(receiverIds: List<Int>) {
        viewModelScope.launch {
            _amount.value = transactionRepository.getOwed(payerId, receiverIds, groupIds)
        }
    }

    fun getAmount(senderId: Int, receiverId: Int? = null, groupId: Int) {
        viewModelScope.launch {
            if (groupId != -1) {
                if (receiverId != null) {
                    _amount.value =
                        transactionRepository.getOwedInGroup(senderId, receiverId, groupId)
                } else {
                    _amount.value = transactionRepository.getOwedInGroup(senderId, groupId)
                }
            } else {
                if (receiverId != null) {
                    _amount.value = transactionRepository.getOwed(senderId, receiverId)
                } else {
                    _amount.value = transactionRepository.getOwed(senderId)
                }
            }
        }
    }

    fun settle(
        receiverIds: List<Int>,
        gotoSplitWiseFragment: () -> Unit
    ) {
        viewModelScope.launch {
            transactionRepository.settle(payerId, receiverIds, groupIds)
        }
        gotoSplitWiseFragment()
    }

    fun settle(
        senderId: Int, receiverId: Int? = null, groupId: Int,
        gotoSplitWiseFragment: () -> Unit
    ) {
        viewModelScope.launch {
            if (groupId != -1) {
                if (receiverId != null) {
                    transactionRepository.settle(senderId, receiverId, groupId)
                } else {
                    transactionRepository.settleAllInGroup(senderId, groupId)
                }
            } else {
                if (receiverId != null) {
                    transactionRepository.settle(senderId, receiverId)
                } else {
                    transactionRepository.settleAll(senderId)
                }
            }
        }
        gotoSplitWiseFragment()
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

}

class SettleUpViewModelFactory(
    private val context: Context,
    private val payerId: Int,
    private val groupIds: List<Int>
) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettleUpViewModel(context, payerId, groupIds) as T
    }
}