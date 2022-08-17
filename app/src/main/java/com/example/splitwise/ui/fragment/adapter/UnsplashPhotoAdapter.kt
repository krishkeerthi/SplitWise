package com.example.splitwise.ui.fragment.adapter

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.databinding.UnsplashPhotoBinding
import com.example.splitwise.model.UnsplashPhoto
import com.example.splitwise.util.downloadBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UnsplashPhotoAdapter(
    private val itemClick: (UnsplashPhoto?) -> Unit
) : PagingDataAdapter<UnsplashPhoto, UnsplashPhotoAdapter.PhotoViewHolder>(PHOTO_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding =
            UnsplashPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        Log.d(ContentValues.TAG, "onCreateViewHolder: called ${itemCount}")

        return PhotoViewHolder(binding).apply {
            binding.unsplashPhoto.setOnClickListener {
                itemClick(getItem(absoluteAdapterPosition))
            }
        }
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val currentItem = getItem(position)
        Log.d(ContentValues.TAG, "onBindViewHolder: ${itemCount}")
        if (currentItem != null)
            holder.bind(currentItem)
    }

    inner class PhotoViewHolder(private val binding: UnsplashPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: UnsplashPhoto) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.i(ContentValues.TAG, "Current thread ${Thread.currentThread().name}")
                val bitmap = downloadBitmap(photo.urls.regular)
                withContext(Dispatchers.Main) {
                    Log.i(
                        ContentValues.TAG,
                        "Current thread in the main dispatcher: ${Thread.currentThread().name}"
                    )
                    binding.unsplashPhoto.setImageBitmap(bitmap)
                }
            }
        }

    }

    companion object {
        private val PHOTO_COMPARATOR = object : DiffUtil.ItemCallback<UnsplashPhoto>() {
            override fun areItemsTheSame(
                oldItem: UnsplashPhoto,
                newItem: UnsplashPhoto
            ): Boolean {
                Log.d(ContentValues.TAG, "areItemsTheSame: inside diffutil")
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: UnsplashPhoto,
                newItem: UnsplashPhoto
            ): Boolean {
                Log.d(ContentValues.TAG, "areItemsTheSame: inside diffutil")
                return oldItem == newItem
            }
        }
    }
}