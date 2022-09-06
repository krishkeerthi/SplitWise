package com.example.splitwise.ui.fragment.settleup

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import com.example.splitwise.model.MemberAndAmount
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

    private val _payeesAndAmounts = MutableLiveData<List<MemberAndAmount>?>()
    val payeesAndAmounts: LiveData<List<MemberAndAmount>?>
        get() = _payeesAndAmounts

    private val _amount = MutableLiveData<Float?>()
    val amount: LiveData<Float?>
        get() = _amount

    private var selectedPayees = mutableSetOf<Member>()

    private var _selectedPayeesCount = MutableLiveData<Int>()
    val selectedPayeesCount: LiveData<Int>
        get() = _selectedPayeesCount

    var tempGroupsId: List<Int> = groupIds
    init {
        getGroups()
        Log.d(TAG, "group id size : ${groupIds.size} ")
    }

    fun fetchData() {
        viewModelScope.launch {
            val member = memberRepository.getMember(payerId)
            _payer.value = member

            val memberIds: List<Int>? = if (groupIds.isEmpty()) {
                transactionRepository.getPayers(payerId)
            } else {
                transactionRepository.getPayers(payerId, groupIds)
            }

            // Test code starts

            if (groupIds.isEmpty()) {
                groupRepository.getGroups()?.let {
                    val groupIds = mutableListOf<Int>()
                    for (group in it)
                        groupIds.add(group.groupId)

                    tempGroupsId = groupIds
                }
            }

            // Test code ends

            memberIds?.let { ids ->
                val members = getMembersFromIds(ids)
                Log.d(TAG, "fetchData: members size ${members?.size}")
                val membersAndAmounts = mutableListOf<MemberAndAmount>()
                members?.let { members ->
                    for (m in members) {
                        val amount = getAmount(m.memberId, tempGroupsId)

                        if (amount != null)
                            membersAndAmounts.add(MemberAndAmount(m, amount))

                        Log.d(TAG, "fetchData: amount ${amount}")
                    }

                    _payeesAndAmounts.value = membersAndAmounts
                }

            }


            // _payees.value = getMembersFromIds(memberIds)
        }
    }

    private fun getGroups() {
        viewModelScope.launch {
            if (groupIds.isNotEmpty()) {
                _groups.value = groupRepository.getGroups(groupIds)
            } else {
                _groups.value = groupRepository.getGroups()
            }
        }
    }

    fun groupIds(): List<Int> {
        val groupIds = mutableListOf<Int>()
        if (groups.value != null) {
            for (group in groups.value!!)
                groupIds.add(group.groupId)
        }

        return groupIds.toList()
    }

    fun getAmount(receiverIds: List<Int>) {
        viewModelScope.launch {
            Log.d(TAG, "getAmount: payerId = $payerId, receiverIds = ${receiverIds} groupIds = $tempGroupsId")
            _amount.value = transactionRepository.getOwed(payerId, receiverIds, tempGroupsId)
        }
    }

    private suspend fun getAmount(receiverId: Int, groupIds: List<Int>) =
        withContext(Dispatchers.IO) {
            transactionRepository.getOwed(payerId, listOf(receiverId), groupIds) // use groups.value
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

    fun setSelectedPayeesCount(){
        _selectedPayeesCount.value = selectedPayees.size
    }

    fun settle(
        receiverIds: List<Int>,
        gotoSplitWiseFragment: () -> Unit
    ) {
        viewModelScope.launch {
            transactionRepository.settle(payerId, receiverIds, tempGroupsId) //groupIds
            Log.d(TAG, "settle: selected groups ${tempGroupsId}")
        }
        Log.d(TAG, "settle: selected fragment not called")
        gotoSplitWiseFragment()
        Log.d(TAG, "settle: selected fragment called")
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

    fun addPayeeToSelected(member: Member) {
        selectedPayees.add(member)
        _selectedPayeesCount.value = selectedPayees.size
        //_selectedPayeesCount.value = _selectedPayeesCount.value?.plus(1) ?: 1

        Log.d(TAG, "addPayeeToSelected: payees added selected count ${_selectedPayeesCount.value}")
    }

    fun removePayeeFromSelected(member: Member) {
        selectedPayees.remove(member)
        _selectedPayeesCount.value = selectedPayees.size
        //_selectedPayeesCount.value = _selectedPayeesCount.value?.minus(1)
        Log.d(TAG, "addPayeeToSelected: payees removed selected count ${_selectedPayeesCount.value}")
    }

    fun selectedPayeesIds(): List<Int>{
        val payeesIds = mutableListOf<Int>()
        for(member in selectedPayees){
            payeesIds.add(member.memberId)
        }
        return payeesIds
    }
    fun getSelectedPayees() = selectedPayees.toList()

    fun clearPayees() {
        selectedPayees = mutableSetOf()
        _selectedPayeesCount.value = selectedPayees.size
        getAmount(selectedPayeesIds())
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