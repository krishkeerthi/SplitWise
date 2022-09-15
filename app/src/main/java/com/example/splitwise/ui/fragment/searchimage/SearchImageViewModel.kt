package com.example.splitwise.ui.fragment.searchimage

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.UnsplashRepository
import com.example.splitwise.ui.fragment.settings.SettingsViewModel
import com.example.splitwise.util.removeIrrelevantWords

class SearchImageViewModel(private val groupName: String): ViewModel() {
   // private val database = SplitWiseRoomDatabase.getInstance(context)

    private val repository: UnsplashRepository= UnsplashRepository()
   // private val groupRepository = GroupRepository(database)

    private val searchQuery= MutableLiveData<String>(DEFAULT_QUERY)

    val photos = searchQuery.switchMap { currentQuery ->
        Log.d(ContentValues.TAG, "viewmodel: inside switchmap, current query is ${currentQuery}")
        //repository.getSearchResults(currentQuery).cachedIn(viewModelScope)
        repository.getSearchResults(currentQuery).cachedIn(viewModelScope)
    }

    init {
        // previously called it from onviewcreated
        showRelatedGroupIcons(removeIrrelevantWords(getGroupName()))
    }

    fun searchPhotos(query: String){
        searchQuery.value = query
    }

    private fun showRelatedGroupIcons(groupName: String) {
        searchPhotos(groupName)
    }

    private fun getGroupName(): String {
        return if(groupName == "")
            "random"
        else
            groupName
    }


    companion object {
        private const val DEFAULT_QUERY = "Chennai"
    }
}

class SearchImageViewModelFactory(private val groupName: String) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchImageViewModel(groupName) as T
    }
}