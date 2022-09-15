package com.example.splitwise.ui.fragment.adapter

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.databinding.UnsplashPhotoBinding
import com.example.splitwise.model.UnsplashPhoto
import com.example.splitwise.util.downloadBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UnsplashPhotoAdapter(
    private val itemClick: (UnsplashPhoto?, View) -> Unit
) : PagingDataAdapter<UnsplashPhoto, UnsplashPhotoAdapter.PhotoViewHolder>(PHOTO_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding =
            UnsplashPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        Log.d(ContentValues.TAG, "onCreateViewHolder: called ${itemCount}")

        return PhotoViewHolder(binding).apply {
            binding.unsplashPhoto.setOnClickListener {
                itemClick(getItem(absoluteAdapterPosition), binding.unsplashPhoto)
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

        private val resources = binding.root.resources
        fun bind(photo: UnsplashPhoto) {

            ViewCompat.setTransitionName(binding.unsplashPhoto, String.
            format(resources.getString(R.string.group_search_image_transition_name), photo.urls))

            binding.unsplashPhoto.setImageDrawable(null)
            binding.unsplashPhoto.visibility = View.INVISIBLE
            binding.indeterminateBar.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                Log.i(ContentValues.TAG, "Current thread ${Thread.currentThread().name}")
                val bitmap = downloadBitmap(photo.urls.regular)
                withContext(Dispatchers.Main) {
                    Log.i(
                        ContentValues.TAG,
                        "Current thread in the main dispatcher: ${Thread.currentThread().name}"
                    )
                    binding.unsplashPhoto.visibility = View.VISIBLE
                    binding.indeterminateBar.visibility = View.INVISIBLE
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