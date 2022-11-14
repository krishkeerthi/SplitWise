package com.example.splitwise.ui.fragment.splitwise

import android.app.Application
import android.content.ContentValues
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.framework.Interactors
import com.example.splitwise.framework.SplitwiseViewModel
import com.example.splitwise.model.MemberPaymentStats
import com.example.splitwise.model.MemberPaymentStatsDetail
import com.example.splitwise.util.getGroupIds
import com.example.splitwise.util.trimGroupModelsToGroups
import com.example.splitwise.util.trimMemberStatsModelToMemberStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MySplitWiseViewModel(application: Application, interactors: Interactors):
    SplitwiseViewModel(application, interactors){

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

    var selectedGroupsText = ""

    var selectedGroupsCardVisibility: MutableLiveData<Int> = MutableLiveData<Int>(View.GONE)

    var removedGroupIds = mutableSetOf<Int>()

    var selectedGroups: Array<Group>? = null // will be changed later
    var selectedGroupsSet = false

    init {


    }

    fun loadGroups(){
        if(selectedGroups != null){
            Log.d(ContentValues.TAG, ": ")
            val groupsList = selectedGroups!!.toMutableList() // manually not null asserted

            // preparing selected group names
            if (groupsList.isNotEmpty()) {
                var groupsText = ""
                for (group in selectedGroups!!) { // manually not null asserted
                    groupsText += "● ${group.groupName} "
                }

                selectedGroupsText = groupsText
                selectedGroupsCardVisibility.value = View.VISIBLE
            }

            fetchData(getGroupIds(groupsList))
        }
        else{
            fetchData()
        }
    }

    fun loadGroupName() {
        viewModelScope.launch {
            _groupName.value = interactors.groupInteractors.getGroup(groupId)?.groupName
        }
    }

    fun fetchData(groupIds: List<Int> = listOf()) {
        Log.d(ContentValues.TAG, "fetchData: ${groupIds.size}")
        viewModelScope.launch {
            val memberStatsModels = if(groupIds.isEmpty()) interactors.transactionInteractors.transactionStats()
            else interactors.transactionInteractors.transactionStatsInGroups(groupIds)


            _membersPaymentStatsDetail.value = memberStatsToDetail(memberStatsModels.trimMemberStatsModelToMemberStats())
            //Log.d(ContentValues.TAG, "getGroupMembersPaymentStats: $memberStats")
            _groups.value = interactors.groupInteractors.getGroups().trimGroupModelsToGroups()
        }
    }

    fun getGroupMembersPaymentStats(groupId: Int? = null) {
        viewModelScope.launch {
            val memberStatsModels = if (groupId != null)
                interactors.transactionInteractors.transactionStatsInGroup(groupId)
            else
                interactors.transactionInteractors.transactionStats()
            //Log.d(ContentValues.TAG, "getGroupMembersPaymentStats: ${memberStats}")
            _membersPaymentStatsDetail.value = memberStatsToDetail(memberStatsModels.trimMemberStatsModelToMemberStats())
        }
    }

    fun getGroupMembersPaymentStats(groupIds: List<Int>) {
        viewModelScope.launch {
            val memberStatsModels = interactors.transactionInteractors.transactionStatsInGroups(groupIds)
            //Log.d(ContentValues.TAG, "getGroupMembersPaymentStats: ${memberStats}")
            _membersPaymentStatsDetail.value = memberStatsToDetail(memberStatsModels.trimMemberStatsModelToMemberStats())
        }
    }

    private suspend fun memberStatsToDetail(memberStats: List<MemberPaymentStats>?): List<MemberPaymentStatsDetail>? {
        return withContext(Dispatchers.IO) {
            if (memberStats != null) {
                val memberPaymentStatsDetails = mutableListOf<MemberPaymentStatsDetail>()

                for (i in memberStats) {
                    interactors.memberInteractors.getMember(i.memberId)?.let {
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