package com.example.splitwise.ui.fragment.createeditgroup

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.util.*

class CreateEditGroupViewModel(
    context: Context,
    var groupId: Int,
    private var selectedMembers: Array<Member>?
) : ViewModel() {


    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupRepository = GroupRepository(database)
    private val memberRepository = MemberRepository(database)


    var tempGroupName = ""
    var newEntry = true // to load data after deleted, when returning from backstack

    var updateMenuVisibility: Boolean = false

    var memberCountChange = false
    var change = false

    //val memberIds = mutableListOf<Int>()

    private val _groupName = MutableLiveData<String?>()

    val groupName: LiveData<String?>
        get() = _groupName

    private val _group = MutableLiveData<Group?>()

    val group: LiveData<Group?>
        get() = _group

    private val _members = MutableLiveData<MutableList<Member>?>()

    val members: LiveData<MutableList<Member>?>
        get() = _members

    var groupMembers = mutableSetOf<Member>()

    val exists = MutableLiveData<Boolean>(false)


    // things to be included
    // entered group name solves, group name edited task, call updateGroupName
    //updated uri, save args.groupIcon here (turn of updating group icon in group icon fragment)
    // hold dummy members and add them during group update(selected members ) already in db & (created members) yet to add in db
    // check whether group members changed above will solve, addMembersNotIncludedInGroup should be called during group update

    var updatedUri: Uri? = null
    var membersNewlyAddedToGroup = mutableSetOf<Member>()

    init {
        Log.d(TAG, "membercheck: init create edit view model called")

        //updatedFetchData(groupId, selectedMembers?.toList())

        selectedMembers?.let {
            addSelectedMembersToGroupMembers(it.toList())
        }

    }

    fun fetchData() {
        Log.d(TAG, "onViewCreated: viewmodel $groupId")

        Log.d(TAG, "onCreateDialog: membercheck create edit viewmodel, members ${members.value}")

        viewModelScope.launch {
            if (groupId != -1 && selectedMembers == null) { // updating group
                Log.d(TAG, "fetchData: update group")
                val group = groupRepository.getGroup(groupId)
                _groupName.value = group?.groupName
                _group.value = group

                val memberIds = groupRepository.getGroupMembers(groupId)?.toMutableList()

                _members.value = getMembersFromIds(memberIds)
            } else if (groupId == -1 && selectedMembers != null) { // selecting member while creating group
                _members.value = selectedMembers!!.toMutableList()  // errors can happen,
                updateFlag()
                Log.d(TAG, "onCreateDialog: membercheck group null, selected members not null")
            } else if (groupId != -1 && selectedMembers != null) { // selecting member while updating group
                val group = groupRepository.getGroup(groupId)
                _groupName.value = group?.groupName
                _group.value = group

                _members.value = selectedMembers!!.toMutableList()  // errors can happen
                addMembersNotIncludedToGroup(listOf())
            } else {
                //_members.value = null
                // do nothing
                Log.d(TAG, "onCreateDialog: membercheck group null,selected members null")
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

    fun updatedFetchData(gId: Int, selectedMembers: List<Member>?) {

        Log.d(TAG, "updatedFetchData: groupid ${gId} selectedMemebers ${selectedMembers}")
        // update group Id
        groupId = gId

        Log.d(TAG, "onViewCreated: viewmodel $groupId")

        Log.d(TAG, "onCreateDialog: membercheck create edit viewmodel, members ${members.value}")

        viewModelScope.launch {
            if (groupId != -1 && selectedMembers == null) { // updating group
                Log.d(TAG, "updatedFetchData: updating group")
                val group = groupRepository.getGroup(groupId)
                _groupName.value = group?.groupName
                _group.value = group

                // later ref
//                if(groupMembers.isNotEmpty()){
//                    _members.value = groupMembers.toMutableList()
//                }
//                else{
                    val memberIds = groupRepository.getGroupMembers(groupId)?.toMutableList()

                    getMembersFromIds(memberIds)?.let {
                        groupMembers = it.toMutableSet()

                        if(membersNewlyAddedToGroup.isNotEmpty()){ // this is called during member update // later ref
                            for(member in membersNewlyAddedToGroup)
                                groupMembers.add(member)
                        }

                        _members.value = groupMembers.toMutableList()
                    }
                //}

            } else if (groupId == -1 && selectedMembers != null) { // choosing member while creating group or adding members

                addSelectedMembersToGroupMembers(selectedMembers)

                _members.value = groupMembers.toMutableList()
                updateFlag()
                Log.d(TAG, "onCreateDialog: membercheck group null, selected members not null")
            } else if (groupId != -1 && selectedMembers != null) { // selecting member while updating group
                // update flag in created group also
                val group = groupRepository.getGroup(groupId)
                _groupName.value = group?.groupName
                _group.value = group

                addSelectedMembersToGroupMembers(selectedMembers)
                addSelectedMembersToNewlyAddedGroupMembers(selectedMembers)

                _members.value = groupMembers.toMutableList()
                // addMembersNotIncludedToGroup(selectedMembers) call this during group update

                // later ref
                groupRepository.getGroupMembers(groupId)?.let {
                    if(groupMembers.size > it.size) // comparing added grp members size to actual grp members size
                        change = true
                }

            } else {
                //_members.value = null
                // do nothing
                Log.d(TAG, "onCreateDialog: membercheck group null,selected members null")
            }
        }
    }

    private fun addSelectedMembersToGroupMembers(selectedMembers: List<Member>) {
        for (member in selectedMembers) {
            groupMembers.add(member)
        }
    }

    private fun addSelectedMembersToNewlyAddedGroupMembers(selectedMembers: List<Member>) {
        for (member in selectedMembers) {
            membersNewlyAddedToGroup.add(member)
        }
    }

    fun reset(): Boolean {
        Log.d(TAG, "reset: called")
        memberCountChange = false
        change = false

        tempGroupName = ""
        //val memberIds = mutableListOf<Int>()

        _groupName.value = null

        _group.value = null

        _members.value = null

        Log.d(TAG, "reset: members ${members.value}")

        //groupMembers.clear()
        groupMembers = mutableSetOf()
        membersNewlyAddedToGroup = mutableSetOf()

        // change updated uri
        updatedUri = null

        exists.value = false
        Log.d(TAG, "reset: ended")

        return true
    }

    private suspend fun addMembersNotIncludedToGroup(selectedMembers: List<Member>) {
        val groupMemberIds = groupRepository.getGroupMembers(groupId)?.toMutableList()

        // since this is called when group id != -1, so atleast 2 members will be there.
        groupMemberIds?.let { memberIds ->

            val sMembers = selectedMembers

            // if (sMembers != null) {
            for (member in sMembers) {
                if (member.memberId !in memberIds) {
                    Log.d(
                        TAG,
                        "addMembersNotIncludedToGroup: newMember id ${member.memberId} existing members ${memberIds}"
                    )
                    groupRepository.addGroupMember(groupId, member.memberId)
                }
            }
            //  }

        }
    }

    private suspend fun newAddMembersNotIncludedToGroup(selectedMembers: List<Member>) {
        viewModelScope.launch {
            val groupMemberIds = groupRepository.getGroupMembers(groupId)?.toMutableList()

            // since this is called when group id != -1, so atleast 2 members will be there.
            groupMemberIds?.let { memberIds ->

                val sMembers = selectedMembers

                for (member in sMembers) {

                    if (member.memberId in 1000..10000) {
                        val mId =
                            memberRepository.addMember(member.name, member.phone, member.memberProfile)
                        val addedMember = memberRepository.getMember(mId)

                        if (addedMember != null) {
                            if(addedMember.memberId !in memberIds)
                                groupRepository.addGroupMember(groupId, addedMember.memberId)
                        }
                    }
                    else{
                        if (member.memberId !in memberIds) {
                            groupRepository.addGroupMember(groupId, member.memberId)
                        }
                    }
                }
            }
        }.join()
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

    fun updateMembers(members: List<Member>) {
        _members.value = members.toMutableList()
    }

    fun addMember(name: String, phoneNumber: Long, uri: Uri?) {
        viewModelScope.launch {

            if (groupId != -1) {
                val memberId = memberRepository.addMember(
                    name,
                    phoneNumber,
                    uri
                ) // needs to be changed, create dummy member not add in db
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
                    existingMembers.add(Member(name, phoneNumber, uri).apply {
                        memberId = Random().nextInt()
                    })
                    _members.value = existingMembers
                } else {
                    _members.value = mutableListOf(Member(name, phoneNumber, uri).apply {
                        memberId = Random().nextInt()
                    })
                }
            }
            if (groupId == -1)
                updateFlag()

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

    fun createGroup(name: String, ownerId: Int, groupIcon: String?, gotoGroupFragment: () -> Unit) {
        viewModelScope.launch {
            val uri = groupIcon?.let { Uri.parse(it) }
            val groupId = groupRepository.createGroup(
                name,
                "description",
                Date(),
                0F,
                uri
            )

            members.value?.let { members ->
                for (i in members) {
                    val member = memberRepository.getMember(i.memberId)
                    if (member == null) {
                        // add to members table
                        val mId = memberRepository.addMember(i.name, i.phone, i.memberProfile)
                        // add to member streak table
                        //memberRepository.addMemberStreak(mId) no need for incrementing streak, just bcaz added to group
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

    fun checkMember(name: String, phoneNumber: Long, uri: Uri?) {
        viewModelScope.launch {
            val memberExist = memberRepository.checkMemberExistence(name, phoneNumber)

            if (!memberExist) {
                Log.d(TAG, "checkMember: checkMemberExistence member does not exist")
                addMember(name, phoneNumber, uri)
            } else {
                Log.d(TAG, "checkMember: checkMemberExistence member exists")
                exists.value = true
            }
        }

    }

    private fun updateFlag() {
        change = true
        memberCountChange = true
    }

    fun updateGroupName(groupName: String, gotoGroupFragment: () -> Unit) {
        if (groupId != -1) {
            viewModelScope.launch {
                groupRepository.updateGroupName(groupId, groupName)
                gotoGroupFragment()
            }
        }
    }

    fun removeMember(groupId: Int, member: Member, onDelete: () -> Unit) {
        if (groupId == -1) {
            groupMembers.remove(member)
            _members.value = groupMembers.toMutableList()

            onDelete()
        }
    }

    fun newUpdateGroupName(
        gotoGroupFragment: () -> Unit,
        groupUpdated: () -> Unit,
        notEdited: () -> Unit
    ) {
//        if (groupId != -1) {
//            if (membersNewlyAddedToGroup.isNotEmpty()
//                || (tempGroupName != _group.value!!.groupName) // !! bcaz groupId != -1
//                || (updatedUri != null && _group.value!!.groupIcon != updatedUri)
//            ) {
                // update group
                viewModelScope.launch {
                    // updating group name
                    if (tempGroupName != _group.value!!.groupName) {
                        groupRepository.updateGroupName(groupId, tempGroupName)
                    }

                    // updating group members
                    if (membersNewlyAddedToGroup.isNotEmpty()) {
                        newAddMembersNotIncludedToGroup(membersNewlyAddedToGroup.toList())
                    }

                    // updating group icon // checking whether uri is updated first, then also checking that uri is different
                    // from previous.
                    if (_group.value!!.groupIcon != updatedUri) { //updatedUri != null &&  later ref
                        if(updatedUri != null)
                            groupRepository.updateGroupIcon(groupId, updatedUri!!)
                        else
                            groupRepository.removeGroupIcon(groupId)
                    }

                    groupUpdated()
                    gotoGroupFragment()
                }
//            } else {
//                notEdited()
//            }
//        }
    }

    fun checkForNewMember(memberId: Int): Boolean {
        for(member in membersNewlyAddedToGroup){
            if(member.memberId == memberId)
                return true
        }
        return false
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