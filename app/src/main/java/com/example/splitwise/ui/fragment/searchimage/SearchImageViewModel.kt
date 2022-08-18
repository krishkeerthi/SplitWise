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

class SearchImageViewModel(): ViewModel() {
   // private val database = SplitWiseRoomDatabase.getInstance(context)

    private val repository: UnsplashRepository= UnsplashRepository()
   // private val groupRepository = GroupRepository(database)

    private val searchQuery= MutableLiveData<String>(DEFAULT_QUERY)

    val photos = searchQuery.switchMap { currentQuery ->
        Log.d(ContentValues.TAG, "viewmodel: inside switchmap, current query is ${currentQuery}")
        //repository.getSearchResults(currentQuery).cachedIn(viewModelScope)
        repository.getSearchResults(currentQuery).cachedIn(viewModelScope)
    }

    fun searchPhotos(query: String){
        searchQuery.value = query
    }

    fun showRelatedGroupIcons(groupName: String) {
        searchPhotos(groupName)
    }

    companion object {
        private const val DEFAULT_QUERY = "Chennai"
    }
}

//class SearchImageViewModelFactory(private val context: Context) :
//    ViewModelProvider.Factory {
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return SearchImageViewModel(context) as T
//    }
//}