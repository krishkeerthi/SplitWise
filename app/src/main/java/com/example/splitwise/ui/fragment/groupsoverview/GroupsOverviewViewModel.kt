package com.example.splitwise.ui.fragment.groupsoverview

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.repository.GroupRepository
import kotlinx.coroutines.launch

class GroupsOverviewViewModel(context: Context) : ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupRepository = GroupRepository(database)

    private val _groups = MutableLiveData<List<Group>?>()

    val groups: LiveData<List<Group>?>
        get() = _groups


    init {
        getGroups()
    }

    private fun getGroups() {
        viewModelScope.launch {
            _groups.value = groupRepository.getGroups()
        }
    }
}

class GroupsOverviewViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupsOverviewViewModel(context) as T
    }
}