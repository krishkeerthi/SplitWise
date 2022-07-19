package com.example.splitwise.ui.fragment.splitwise

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import com.example.splitwise.model.MemberPaymentStats
import com.example.splitwise.model.MemberPaymentStatsDetail
import com.example.splitwise.ui.fragment.addexpense.AddExpenseViewModel
import kotlinx.coroutines.launch

class SplitWiseViewModel(context: Context): ViewModel() {

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

    init{
        viewModelScope.launch {
            val memberStats = transactionRepository.transactionStats()
            _membersPaymentStatsDetail.value = memberStatsToDetail(memberStats)

            _groups.value = groupRepository.getGroups()
        }
    }

    fun getGroupMembersPaymentStats(groupId: Int){
        viewModelScope.launch {
            val memberStats = transactionRepository.transactionStats(groupId)
            _membersPaymentStatsDetail.value = memberStatsToDetail(memberStats)
        }
    }

    private fun  memberStatsToDetail(memberStats: List<MemberPaymentStats>?): List<MemberPaymentStatsDetail>?{
        return if(memberStats != null){
            val memberPaymentStatsDetails = mutableListOf<MemberPaymentStatsDetail>()

            viewModelScope.launch {
                for(i in memberStats){
                    memberRepository.getMember(i.memberId)?.let {
                        memberPaymentStatsDetails.add(
                            MemberPaymentStatsDetail(
                                i.memberId,
                                it.name,
                                i.amountLend,
                                i.amountOwed
                            )
                        )
                    }
                }
            }
            memberPaymentStatsDetails
        }
        else
            null
    }
}

class SplitWiseViewModelFactory(private val context: Context):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SplitWiseViewModel(context) as T
    }
}