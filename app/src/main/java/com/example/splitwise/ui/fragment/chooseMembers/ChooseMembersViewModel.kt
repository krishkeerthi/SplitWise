package com.example.splitwise.ui.fragment.chooseMembers

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.model.MemberAndStreak
import kotlinx.coroutines.launch

class ChooseMembersViewModel(context: Context, selectedMembers: Array<Member>?) :
    ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)

    private val _membersAndStreaks = MutableLiveData<MutableList<MemberAndStreak>?>()

    val membersAndStreaks: LiveData<MutableList<MemberAndStreak>?>
        get() = _membersAndStreaks

    private val selectedMembersList = selectedMembers?.toMutableList() ?: mutableListOf()

    private var _selectedMembersCount = MutableLiveData<Int>()
    val selectedMembersCount: LiveData<Int>
        get() = _selectedMembersCount


    var checkedMembers = mutableSetOf<Member>()
    init {
        _selectedMembersCount.value = 0
    }

    fun fetchData() {
        viewModelScope.launch {
            val members = memberRepository.getMembers()?.toMutableList() ?: mutableListOf()
            val remainingMembers = mutableListOf<Member>()

            if (selectedMembersList.isEmpty())
                sortByStreak(members)
            else {
                for (member in members) {
                    if (member !in selectedMembersList)
                        remainingMembers.add(member)
                }

                sortByStreak(remainingMembers)

                // _members.value = sortedMembers as MutableList<Member>
            }
        }
    }

    fun addMemberToSelected(member: Member) {
        //selectedMembersList.add(member)
        if(member !in checkedMembers) {
            checkedMembers.add(member)
            _selectedMembersCount.value = _selectedMembersCount.value?.plus(1)
        }
    }

    fun removeMemberFromSelected(member: Member) {
        //selectedMembersList.remove(member)
        if(member in checkedMembers) {
            checkedMembers.remove(member)
            _selectedMembersCount.value = _selectedMembersCount.value?.minus(1)
        }
    }

    fun getSelectedMembers() = selectedMembersList.toTypedArray()

    fun getCheckedMembers() = checkedMembers.toTypedArray()

    private suspend fun sortByStreak(members: List<Member>) {
        viewModelScope.launch {
            val ids = mutableListOf<Int>()
            for (member in members) {
                ids.add(member.memberId)
            }

            val membersAndStreaks = mutableListOf<MemberAndStreak>()
            memberRepository.getMembersStreak(ids)?.let { membersStreak ->
                for (memberStreak in membersStreak) {
                    membersAndStreaks.add(
                        MemberAndStreak(
                            memberRepository.getMember(memberStreak.memberId)!!,
                            memberStreak.streak
                        )
                    )
                }
            }

            _membersAndStreaks.value = membersAndStreaks
        }.join()
    }

    fun checkedMembersIds(): List<Int>{
        val membersIds = mutableListOf<Int>()
        for(member in checkedMembers){
            membersIds.add(member.memberId)
        }
        return membersIds
    }

    //fun getCheckedMembers() = checkedMembers.toList()
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