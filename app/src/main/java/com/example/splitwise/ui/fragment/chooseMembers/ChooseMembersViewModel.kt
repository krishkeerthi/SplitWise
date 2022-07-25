package com.example.splitwise.ui.fragment.chooseMembers

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.MemberRepository
import kotlinx.coroutines.launch

class ChooseMembersViewModel(context: Context, private val selectedMembers: Array<Member>?) :
    ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)

    private val _members = MutableLiveData<MutableList<Member>?>()

    val members: LiveData<MutableList<Member>?>
        get() = _members

    private val selectedMembersList = selectedMembers?.toMutableList() ?: mutableListOf()

    private var _selectedMembersCount= MutableLiveData<Int>()
    val selectedMembersCount: LiveData<Int>
    get() = _selectedMembersCount

    init {
        _selectedMembersCount.value = 0
    }

    fun fetchData() {
        viewModelScope.launch {
            val members = memberRepository.getMembers()?.toMutableList() ?: mutableListOf()
            val remainingMembers = mutableListOf<Member>()

            if (selectedMembersList.isEmpty())
                _members.value = members
            else {
                for (member in members) {
                    if (member !in selectedMembersList)
                        remainingMembers.add(member)
                }

                _members.value = remainingMembers
            }
        }
    }

    fun addMemberToSelected(member: Member){
        selectedMembersList.add(member)
        _selectedMembersCount.value = _selectedMembersCount.value?.plus(1)
    }

    fun removeMemberFromSelected(member: Member){
        selectedMembersList.remove(member)
        _selectedMembersCount.value = _selectedMembersCount.value?.minus(1)
    }

    fun getSelectedMembers() = selectedMembersList.toTypedArray()
}

class ChooseMembersViewModelFactory(
    private val context: Context,
    private val selectedMembers: Array<Member>?
) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChooseMembersViewModel(context, selectedMembers) as T
    }
}