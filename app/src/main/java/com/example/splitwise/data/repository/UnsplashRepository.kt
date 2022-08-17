package com.example.splitwise.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.splitwise.model.UnsplashPhoto
import com.example.splitwise.pagingsource.UnsplashPagingSource

class UnsplashRepository {

    fun getSearchResults(query: String): LiveData<PagingData<UnsplashPhoto>> {
        return Pager(
            config = PagingConfig(
                pageSize =20,
                maxSize = 100,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {UnsplashPagingSource(query)}
        ).liveData
    }
}