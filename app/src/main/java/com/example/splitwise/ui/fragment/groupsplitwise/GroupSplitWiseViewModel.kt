package com.example.splitwise.ui.fragment.groupsplitwise

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import com.example.splitwise.model.MemberPaymentStats
import com.example.splitwise.model.MemberPaymentStatsDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupSplitWiseViewModel(context: Context, private val groupId: Int) : ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)
    private val transactionRepository = TransactionRepository(database)

    private val _membersPaymentStatsDetail = MutableLiveData<List<MemberPaymentStatsDetail>?>()

    val membersPaymentStatsDetail: LiveData<List<MemberPaymentStatsDetail>?>
        get() = _membersPaymentStatsDetail

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            val memberStats = transactionRepository.transactionStats(listOf(groupId))
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

class GroupSplitWiseViewModelFactory(private val context: Context, private val groupId: Int) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupSplitWiseViewModel(context, groupId) as T
    }
}

