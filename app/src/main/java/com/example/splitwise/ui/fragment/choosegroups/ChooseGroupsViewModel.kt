package com.example.splitwise.ui.fragment.choosegroups

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.repository.GroupRepository
import kotlinx.coroutines.launch

class ChooseGroupsViewModel(context: Context)
    : ViewModel(){

    private val database = SplitWiseRoomDatabase.getInstance(context)

    private val groupRepository = GroupRepository(database)

    private val _groups=  MutableLiveData<List<Group>?>()
    val groups: LiveData<List<Group>?>
            get() = _groups

    private var selectedGroups = mutableListOf<Group>()

    private var _selectedGroupsCount = MutableLiveData<Int>()
    val selectedGroupsCount: LiveData<Int>
        get() = _selectedGroupsCount

    init {
        fetchData()
        database
    }

    fun fetchData(){
        viewModelScope.launch {
            _groups.value = groupRepository.getGroups()
            Log.d(TAG, "fetchData: ${_groups.value}")
        }
    }

    fun addGroupToSelected(group: Group) {
        selectedGroups.add(group)
        _selectedGroupsCount.value = _selectedGroupsCount.value?.plus(1) ?: 1
    }

    fun removeGroupFromSelected(group: Group) {
        selectedGroups.remove(group)
        _selectedGroupsCount.value = _selectedGroupsCount.value?.minus(1)
    }

    fun selectedGroupIds(): List<Int>{
        val groupIds = mutableListOf<Int>()
        for(group in selectedGroups){
            groupIds.add(group.groupId)
        }
        return groupIds
    }

    fun getSelectedGroups() = selectedGroups.toList()



}

class ChooseGroupsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChooseGroupsViewModel(context) as T
    }
}