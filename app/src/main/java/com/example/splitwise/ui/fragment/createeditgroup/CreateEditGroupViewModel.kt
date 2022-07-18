package com.example.splitwise.ui.fragment.createeditgroup

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.ui.fragment.addexpense.AddExpenseViewModel
import kotlinx.coroutines.launch
import java.util.*

class CreateEditGroupViewModel(context: Context, val groupId: Int?): ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupRepository = GroupRepository(database)
    private val memberRepository = MemberRepository(database)

    private val memberIds = mutableListOf<Int>()

    private val _groupName = MutableLiveData<String?>()

    val groupName: LiveData<String?>
        get() = _groupName

    private val _members = MutableLiveData<List<Member>?>()

    val members: LiveData<List<Member>?>
        get() = _members

    init {
        if(groupId != null){
            viewModelScope.launch {
                val group = groupRepository.getGroup(groupId)
                _groupName.value = group?.groupName

                val memberIds = groupRepository.getGroupMembers(groupId)
                _members.value = getMembersFromIds(memberIds)
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

    fun addMember(name: String, phoneNumber: Long){
        if(groupId == null){
            viewModelScope.launch {
                val memberId = memberRepository.addMember(name, phoneNumber)
                memberIds.add(memberId)
            }
        }
        else{
            viewModelScope.launch {
                val memberId = memberRepository.addMember(name, phoneNumber)
                groupRepository.addGroupMember(groupId, memberId)
            }
        }
    }

    fun createGroup(name: String){
        viewModelScope.launch {
            val groupId = groupRepository.createGroup(
                name,
                "description",
                Date(),
                0F
            )

            for(memberId in memberIds)
                groupRepository.addGroupMember(groupId, memberId)

        }
    }
}

class CreateEditGroupViewModelFactory(private val context: Context, private val groupId: Int?):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateEditGroupViewModel(context, groupId) as T
    }
}