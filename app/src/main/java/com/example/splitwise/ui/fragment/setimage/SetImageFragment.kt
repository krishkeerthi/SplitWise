package com.example.splitwise.ui.fragment.setimage

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentSetImageBinding
import com.example.splitwise.util.downloadBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class SetImageFragment : Fragment() {

    private lateinit var binding: FragmentSetImageBinding
    private val args:  SetImageFragmentArgs by navArgs()

    private var msg = ""
    private var lastMsg = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSetImageBinding.bind(view)

        CoroutineScope(Dispatchers.IO).launch {
            Log.i(ContentValues.TAG, "Current thread ${Thread.currentThread().name}")
            val url = args.imageUrl
            val bitmap = downloadBitmap(url!!)
            withContext(Dispatchers.Main) {
                Log.i(ContentValues.TAG, "Current thread in the main dispatcher: ${Thread.currentThread().name}")
                binding.unsplashPhotoImageView.setImageBitmap(bitmap)
            }
        }

        // menu
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.set_image_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.download_menu -> {
                //saveImage()
                downloadImage()
                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }
    }

    @SuppressLint("Range")
    private fun downloadImage(){
        val url = args.imageUrl

        val directory = File(Environment.DIRECTORY_PICTURES)

        Log.d(ContentValues.TAG, "downloadImage: directory ${directory}")
        val directory1 = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )//.toString() + File.separator //+ IMAGES_FOLDER_NAME

        Log.d(ContentValues.TAG, "downloadImage: directory1 ${directory1}")
        // val file = File(imagesDir, "IMG${Date().time}.png")

        if(!directory.exists())
            directory.mkdirs()

        val downloadManager = requireActivity().getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(url?.substring(url.lastIndexOf("/") + 1))
                .setDescription("")
                .setDestinationInExternalPublicDir(
                    directory.toString(),
                    "IMG${Date().time}.png"
                    // url?.substring(url.lastIndexOf("/")+ 1)
                )
        }

        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)

        Thread {
            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                msg = statusMessage(url!!, directory, status) // not null asserting url
                if (msg != lastMsg) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                    lastMsg = msg ?: ""
                }
                cursor.close()
            }
        }.start()

    }

    private fun statusMessage(url: String, directory: File, status: Int): String {
        var msg = ""
        msg = when (status) {
            DownloadManager.STATUS_FAILED -> "Download has been failed, please try again"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Downloading..."
            DownloadManager.STATUS_SUCCESSFUL -> "Image downloaded successfully in $directory" + File.separator + url.substring(
                url.lastIndexOf("/") + 1
            )
            else -> "There's nothing to download"
        }
        return msg
    }
}