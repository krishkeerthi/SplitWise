package com.example.splitwise.ui.fragment.choosepayees

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.model.MemberAndAmount
import kotlinx.coroutines.launch

class ChoosePayeesViewModel(private val payeesAndAmounts: MutableList<MemberAndAmount>)
    : ViewModel(){

    private val _members=  MutableLiveData<List<Member>?>()
    val members: LiveData<List<Member>?>
        get() = _members

    private val _membersAndAmounts=  MutableLiveData<List<MemberAndAmount>?>()
    val membersAndAmounts: LiveData<List<MemberAndAmount>?>
        get() = _membersAndAmounts

    private var selectedMembers = mutableListOf<Member>()

    private var _selectedMembersCount = MutableLiveData<Int>()
    val selectedMembersCount: LiveData<Int>
        get() = _selectedMembersCount

    init {
        fetchData()
    }

    fun fetchData(){
//        viewModelScope.launch {
//            _members.value = selectedPayees
//            Log.d(ContentValues.TAG, "fetchData: ${_members.value}")
//        }

//        val selectedPayees = mutableListOf<Member>()
//        for(payee in payeesAndAmounts){
//            selectedPayees.add(payee.member)
//        }

        //_members.value = selectedPayees

        _membersAndAmounts.value = payeesAndAmounts
    }

    fun addMemberToSelected(member: Member) {
        selectedMembers.add(member)
        _selectedMembersCount.value = _selectedMembersCount.value?.plus(1) ?: 1
    }

    fun removeMemberFromSelected(member: Member) {
        selectedMembers.remove(member)
        _selectedMembersCount.value = _selectedMembersCount.value?.minus(1)
    }

    fun getSelectedMembers() = selectedMembers.toList()
}

class ChoosePayeesViewModelFactory(
    private val payeesAndAmounts: MutableList<MemberAndAmount>
) : ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChoosePayeesViewModel(payeesAndAmounts) as T
    }
}
