package com.example.splitwise.ui.fragment.createeditgroup

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.ui.fragment.addexpense.AddExpenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CreateEditGroupViewModel(context: Context, val groupId: Int): ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupRepository = GroupRepository(database)
    private val memberRepository = MemberRepository(database)

    val memberIds = mutableListOf<Int>()

    private val _groupName = MutableLiveData<String?>()

    val groupName: LiveData<String?>
        get() = _groupName

    private val _members = MutableLiveData<MutableList<Member>?>()

    val members: LiveData<MutableList<Member>?>
        get() = _members

    init {
        Log.d(TAG, "onViewCreated: viewmodel ${groupId}")
        if(groupId != -1){
            viewModelScope.launch {
                val group = groupRepository.getGroup(groupId)
                _groupName.value = group?.groupName

                val memberIds = groupRepository.getGroupMembers(groupId)
                _members.value = getMembersFromIds(memberIds)
            }
        }

    }

    private suspend fun getMembersFromIds(memberIds: List<Int>?): MutableList<Member>? {
        return withContext(Dispatchers.IO){
            if(memberIds != null){
                val members = mutableListOf<Member>()

                for(id in memberIds) {
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

    fun addMember(name: String, phoneNumber: Long){
        viewModelScope.launch {
            val memberId = memberRepository.addMember(name, phoneNumber)
            memberRepository.getMember(memberId)?.let {
                _members.value?.add(it)
            }

            if(groupId == -1){
                memberIds.add(memberId)
                Log.d(TAG, "addMember: member added to list")
            }
            else{
                groupRepository.addGroupMember(groupId, memberId)
                Log.d(TAG, "addMember: member added to group, caz group id is known")
            }
        }

    }

    fun createGroup(name: String, ownerId: Int){
        viewModelScope.launch {
            val groupId = groupRepository.createGroup(
                name,
                "description",
                Date(),
                0F
            )

            for(memberId in memberIds)
                groupRepository.addGroupMember(groupId, memberId)

            if(ownerId != -1)
                groupRepository.addGroupMember(groupId, ownerId)
        }
    }
}

class CreateEditGroupViewModelFactory(private val context: Context, private val groupId: Int):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateEditGroupViewModel(context, groupId) as T
    }
}