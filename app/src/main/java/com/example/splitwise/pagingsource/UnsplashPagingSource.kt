package com.example.splitwise.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.splitwise.api.UnsplashApi
import com.example.splitwise.model.UnsplashPhoto
import java.io.IOException
import java.lang.Exception


private const val UNSPLASH_STARTING_PAGE_INDEX = 1

class UnsplashPagingSource(private val query: String):
    PagingSource<Int, UnsplashPhoto>(){

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UnsplashPhoto> {
        val position = params.key ?: UNSPLASH_STARTING_PAGE_INDEX
        return try {
            val response = UnsplashApi.searchPhotos(query, position, params.loadSize)
            val photos = response.results

            LoadResult.Page(
                data = photos,
                prevKey = if (position == UNSPLASH_STARTING_PAGE_INDEX) null else position - 1,
                nextKey = if (photos.isEmpty()) null else position + 1
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, UnsplashPhoto>): Int? {
        return 1
    }
}