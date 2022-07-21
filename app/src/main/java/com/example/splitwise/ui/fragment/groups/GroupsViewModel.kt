package com.example.splitwise.ui.fragment.groups

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.ui.fragment.addexpense.AddExpenseViewModel
import kotlinx.coroutines.launch

class GroupsViewModel(context: Context): ViewModel() {
    private val groupsRepository = GroupRepository(SplitWiseRoomDatabase.getInstance(context))

    private val _groups = MutableLiveData<List<Group>?>()

    val groups: LiveData<List<Group>?>
        get() = _groups

    fun fetchData(){
        Log.d(TAG, "groupsviewmodel: called")
        viewModelScope.launch {
            _groups.value = groupsRepository.getGroups()
        }
    }
}

class GroupsViewModelFactory(private val context: Context):
    ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupsViewModel(context) as T
    }
}
