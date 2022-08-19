package com.example.splitwise.ui.fragment.setimage

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.repository.GroupRepository
import kotlinx.coroutines.launch

class SetImageViewModel(context: Context, val imageUrl: String?, val groupId: Int) : ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupRepository = GroupRepository(database)

    init {

    }

//    private fun setImage(){
//        CoroutineScope(Dispatchers.IO).launch {
//            Log.i(ContentValues.TAG, "Current thread ${Thread.currentThread().name}")
//            val url = args.imageUrl
//            val bitmap = downloadBitmap(url!!)
//            withContext(Dispatchers.Main) {
//                Log.i(
//                    ContentValues.TAG,
//                    "Current thread in the main dispatcher: ${Thread.currentThread().name}"
//                )
//                binding.unsplashPhotoImageView.setImageBitmap(bitmap)
//            }
//        }
//    }

    fun updateGroupIcon(uri: Uri, navigation: () -> Unit) {
        viewModelScope.launch {
            groupRepository.updateGroupIcon(groupId, uri)
            navigation()
        }
    }

}

class SetImageViewModelFactory(
    private val context: Context,
    private val imageUrl: String?,
    private val groupId: Int
) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SetImageViewModel(context, imageUrl, groupId) as T
    }
}