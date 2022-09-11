package com.example.splitwise.ui.fragment.splitwise

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import com.example.splitwise.model.MemberPaymentStats
import com.example.splitwise.model.MemberPaymentStatsDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplitWiseViewModel(context: Context) : ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)
    private val transactionRepository = TransactionRepository(database)
    private val groupRepository = GroupRepository(database)

    private val _membersPaymentStatsDetail = MutableLiveData<List<MemberPaymentStatsDetail>?>()

    val membersPaymentStatsDetail: LiveData<List<MemberPaymentStatsDetail>?>
        get() = _membersPaymentStatsDetail

    private val _groups = MutableLiveData<List<Group>?>()

    val groups: LiveData<List<Group>?>
        get() = _groups

    var groupId: Int = -1

    private val _groupName = MutableLiveData<String?>()

    val groupName: LiveData<String?>
        get() = _groupName

    init {
       // fetchData()
    }

    fun loadGroupName() {
        viewModelScope.launch {
            _groupName.value = groupRepository.getGroup(groupId)?.groupName
        }
    }

    fun fetchData(groupIds: List<Int> = listOf()) {
        Log.d(TAG, "fetchData: ${groupIds.size}")
        viewModelScope.launch {
            val memberStats = if(groupIds.isEmpty()) transactionRepository.transactionStats()
            else transactionRepository.transactionStats(groupIds)

            _membersPaymentStatsDetail.value = memberStatsToDetail(memberStats)
            Log.d(TAG, "getGroupMembersPaymentStats: $memberStats")
            _groups.value = groupRepository.getGroups()
        }
    }

    fun getGroupMembersPaymentStats(groupId: Int? = null) {
        viewModelScope.launch {
            val memberStats = if (groupId != null)
                transactionRepository.transactionStats(groupId)
            else
                transactionRepository.transactionStats()
            Log.d(TAG, "getGroupMembersPaymentStats: ${memberStats}")
            _membersPaymentStatsDetail.value = memberStatsToDetail(memberStats)
        }
    }

    fun getGroupMembersPaymentStats(groupIds: List<Int>) {
        viewModelScope.launch {
            val memberStats = transactionRepository.transactionStats(groupIds)
            Log.d(TAG, "getGroupMembersPaymentStats: ${memberStats}")
            _membersPaymentStatsDetail.value = memberStatsToDetail(memberStats)
        }
    }

    private suspend fun memberStatsToDetail(memberStats: List<MemberPaymentStats>?): List<MemberPaymentStatsDetail>? {
        return withContext(Dispatchers.IO) {
            if (memberStats != null) {
                val memberPaymentStatsDetails = mutableListOf<MemberPaymentStatsDetail>()

                for (i in memberStats) {
                    memberRepository.getMember(i.memberId)?.let {
                        memberPaymentStatsDetails.add(
                            MemberPaymentStatsDetail(
                                i.memberId,
                                it.name,
                                it.memberProfile,
                                i.amountLend,
                                i.amountOwed
                            )
                        )
                    }
                }
                memberPaymentStatsDetails
            } else
                null
        }
    }
}

class SplitWiseViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SplitWiseViewModel(context) as T
    }
}