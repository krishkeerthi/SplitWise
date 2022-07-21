package com.example.splitwise.ui.fragment.chooseMembers

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.MemberRepository
import kotlinx.coroutines.launch

class ChooseMembersViewModel(context: Context, private val memberIds: IntArray) : ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)

    private val _members = MutableLiveData<MutableList<Member>?>()

    val members: LiveData<MutableList<Member>?>
        get() = _members

    fun fetchData() {
        viewModelScope.launch {
            val members = memberRepository.getMembers()?.toMutableList() ?: mutableListOf()
            val remainingMembers = mutableListOf<Member>()

            for (member in members) {
                if (member.memberId !in memberIds)
                    remainingMembers.add(member)
            }

            _members.value = remainingMembers
        }
    }
}

class ChooseMembersViewModelFactory(private val context: Context, private val memberIds: IntArray) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChooseMembersViewModel(context, memberIds) as T
    }
}