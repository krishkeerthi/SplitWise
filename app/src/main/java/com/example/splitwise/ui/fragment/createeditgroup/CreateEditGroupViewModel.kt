package com.example.splitwise.ui.fragment.createeditgroup

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CreateEditGroupViewModel(
    context: Context,
    val groupId: Int,
    val selectedMembers: Array<Member>?
) : ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupRepository = GroupRepository(database)
    private val memberRepository = MemberRepository(database)

    //val memberIds = mutableListOf<Int>()

    private val _groupName = MutableLiveData<String?>()

    val groupName: LiveData<String?>
        get() = _groupName

    private val _members = MutableLiveData<MutableList<Member>?>()

    val members: LiveData<MutableList<Member>?>
        get() = _members


    fun fetchData() {
        Log.d(TAG, "onViewCreated: viewmodel $groupId")

        viewModelScope.launch {
            if (groupId != -1 && selectedMembers == null) {
                val group = groupRepository.getGroup(groupId)
                _groupName.value = group?.groupName

                val memberIds = groupRepository.getGroupMembers(groupId)?.toMutableList()

                _members.value = getMembersFromIds(memberIds)
            } else if (groupId == -1 && selectedMembers != null) {
                _members.value = selectedMembers.toMutableList()
            } else if (groupId != -1 && selectedMembers != null) {
                _members.value = selectedMembers.toMutableList()
                addMembersNotIncludedToGroup()
            } else {
                _members.value = null
            }
//            if(groupId != -1 && memberId != -1){
//                val group = groupRepository.getGroup(groupId)
//                _groupName.value = group?.groupName
//
//                val memberIds = groupRepository.getGroupMembers(groupId)?.toMutableList()
//
//                memberIds?.add(memberId)
//
//                _members.value = getMembersFromIds(memberIds)
//            }
//            else if(groupId != -1 && memberId == -1){
//                val group = groupRepository.getGroup(groupId)
//                _groupName.value = group?.groupName
//
//                val memberIds = groupRepository.getGroupMembers(groupId)?.toMutableList()
//
//                _members.value = getMembersFromIds(memberIds)
//            }
//            else if(groupId == -1 && memberId != -1){
//                _members.value = getMembersFromIds(mutableListOf(memberId))
//            }
//            else{
//                // Nothing to do here
//            }
        }
    }

    private suspend fun addMembersNotIncludedToGroup() {
        val groupMemberIds = groupRepository.getGroupMembers(groupId)?.toMutableList()

        // since this is called when group id != -1, so atleast 2 members will be there.
        groupMemberIds?.let { memberIds ->

            val sMembers = selectedMembers?.toList()

            if (sMembers != null) {
                for (member in sMembers) {
                    if (member.memberId !in memberIds)
                        groupRepository.addGroupMember(groupId, member.memberId)
                }
            }

        }
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


    fun addMember(name: String, phoneNumber: Long) {
        viewModelScope.launch {

            if (groupId != -1) {
                val memberId = memberRepository.addMember(name, phoneNumber)
                memberRepository.getMember(memberId)?.let {
                    Log.d(TAG, "addMember: $it")
                    val existingMembers = members.value

                    if (existingMembers != null) {
                        existingMembers.add(it)
                        _members.value = existingMembers
                    } else {
                        _members.value = mutableListOf(it)
                    }

                }

                groupRepository.addGroupMember(groupId, memberId)
            } else {
                val existingMembers = members.value

                if (existingMembers != null) {
                    existingMembers.add(Member(name, phoneNumber).apply {
                        memberId = Random().nextInt()
                    })
                    _members.value = existingMembers
                } else {
                    _members.value = mutableListOf(Member(name, phoneNumber).apply {
                        memberId = Random().nextInt()
                    })
                }
            }


//            if(groupId == -1){
//                memberIds.add(memberId)
//                Log.d(TAG, "addMember: member added to list")
//            }
//            else{
//                groupRepository.addGroupMember(groupId, memberId)
//                Log.d(TAG, "addMember: member added to group, caz group id is known")
//            }
        }

    }

    fun createGroup(name: String, ownerId: Int, gotoGroupFragment: () -> Unit) {
        viewModelScope.launch {
            val groupId = groupRepository.createGroup(
                name,
                "description",
                Date(),
                0F
            )

            members.value?.let { members ->
                for (i in members) {
                    val member = memberRepository.getMember(i.memberId)
                    if (member == null) {
                        // add to members table
                        val mId = memberRepository.addMember(i.name, i.phone)
                        // add to member streak table
                        memberRepository.addMemberStreak(mId)
                        groupRepository.addGroupMember(groupId, mId)
                    } else
                        groupRepository.addGroupMember(groupId, member.memberId)
                }
            }

//            if(ownerId != -1)
//                groupRepository.addGroupMember(groupId, ownerId)

            Log.d(TAG, "createGroup: group created")

            gotoGroupFragment()
        }
    }

    fun getMembers(): List<Int> {
        val memberIds = mutableListOf<Int>()
        members.value?.let {
            for (member in it)
                memberIds.add(member.memberId)
        }
        return memberIds
    }

    fun getMembersSize(): Int {
        return if (members.value != null) {
            members.value!!.size
        } else
            0
    }
}

class CreateEditGroupViewModelFactory(
    private val context: Context, private val groupId: Int,
    private val selectedMembers: Array<Member>?
) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateEditGroupViewModel(context, groupId, selectedMembers) as T
    }
}