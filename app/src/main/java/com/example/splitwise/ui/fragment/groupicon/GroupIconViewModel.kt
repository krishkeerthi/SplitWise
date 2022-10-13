package com.example.splitwise.ui.fragment.groupicon

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.repository.GroupRepository
import kotlinx.coroutines.launch

class GroupIconViewModel(context: Context, val groupId: Int) : ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupRepository = GroupRepository(database)

    fun updateGroupIcon(uri: Uri, navigation: () -> Unit) {
        viewModelScope.launch {
            groupRepository.updateGroupIcon(groupId, uri)
            navigation()
        }
    }

    fun removeGroupIcon(navigation: () -> Unit) {
        viewModelScope.launch {
            groupRepository.removeGroupIcon(groupId)
            navigation()
        }
    }

    fun updateGroupIcon(uri: Uri){
        viewModelScope.launch {
            groupRepository.updateGroupIcon(groupId, uri)
        }
    }
}

class GroupIconViewModelFactory(
    private val context: Context,
    private val groupId: Int
) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupIconViewModel(context, groupId) as T
    }
}