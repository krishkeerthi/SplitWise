package com.example.splitwise.ui.fragment.searchimage

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.splitwise.data.repository.UnsplashRepository

class SearchImageViewModel: ViewModel() {
    private val repository: UnsplashRepository= UnsplashRepository()

    private val searchQuery= MutableLiveData<String>(DEFAULT_QUERY)

    val photos = searchQuery.switchMap { currentQuery ->
        Log.d(ContentValues.TAG, "viewmodel: inside switchmap, current query is ${currentQuery}")
        //repository.getSearchResults(currentQuery).cachedIn(viewModelScope)
        repository.getSearchResults(currentQuery)
    }

    fun searchPhotos(query: String){
        searchQuery.value = query
    }

    companion object {
        private const val DEFAULT_QUERY = "Chennai"
    }
}