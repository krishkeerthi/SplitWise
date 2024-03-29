package com.example.splitwise.ui.fragment.setimage

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentSetImageBinding
import com.example.splitwise.util.downloadBitmap
import com.example.splitwise.util.hasImage
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class SetImageFragment : Fragment() {

    private lateinit var binding: FragmentSetImageBinding
    private val args: SetImageFragmentArgs by navArgs()

    private val viewModel: SetImageViewModel by viewModels{
        SetImageViewModelFactory(requireContext(), args.imageUrl, args.groupId)
    }

    private var msg = ""
    private var lastMsg = ""

    private var downloadLater = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_container
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = resources.getColor(R.color.background)//Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.background))
        }

        val callback = object : OnBackPressedCallback(true /* enabled by default */) {

            override fun handleOnBackPressed() {
                if (viewModel.downloading) {
                    Toast.makeText(requireContext(), getString(R.string.downloading_group_icon), Toast.LENGTH_SHORT).show()
                } else {
                    NavHostFragment.findNavController(this@SetImageFragment)
                        .popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.group_icon)
        return inflater.inflate(R.layout.fragment_set_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetImageBinding.bind(view)

        //viewModel.setImage
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Current thread ${Thread.currentThread().name}")
            val url = args.imageUrl
            val bitmap = downloadBitmap(url!!)

            // turn off spinner here

            withContext(Dispatchers.Main) {
                binding.indeterminateBar.visibility = View.GONE
                Log.d(TAG,
                    "Current thread in the main dispatcher: ${Thread.currentThread().name}"
                )
                Log.d(TAG, "downloadImage: has image not set")
                binding.unsplashPhotoImageView.setImageBitmap(bitmap)

                // if done option menu is clicked while image was loading
                if(downloadLater) {
                    Log.d(TAG, "downloadImage: has image")
                    downloadImage()
                }
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
        return when (item.itemId) {
            R.id.done_menu -> {
                downloadImage()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    @SuppressLint("Range")
    private fun downloadImage() {
        viewModel.downloading = true
        if(hasImage(binding.unsplashPhotoImageView)) {

            Log.d(TAG, "downloadImage: has image, done clicked")
            val url = args.imageUrl

            val directory = File(Environment.DIRECTORY_PICTURES)
            //val directory = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            Log.d(TAG, "downloadImage: directory ${directory}")
            val fileName = "IMG${Date().time}.png"

            // Environment.getExternalStoragePublicDirectory()
//        val testUri = File(Environment.getExternalStoragePublicDirectory(
//            Environment.DIRECTORY_PICTURES
//        ), fileName).toUri()

            //val x = File(Environment.getExternalStorageDirectory(), fileName).toUri()
            val testUri = File(
                requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!,
                fileName
            ).toUri()

            Log.d(TAG, "downloadImage: test uri ${testUri}")

            val iconUri = Uri.parse(directory.toString() + fileName)
            // Log.d(TAG, "downloadImage: uri is ${iconUri}")

            Log.d(ContentValues.TAG, "downloadImage: directory ${directory}")
            val directory1 = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )//.toString() + File.separator //+ IMAGES_FOLDER_NAME

            //val directory1 = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            //Log.d(ContentValues.TAG, "downloadImage: directory1 ${directory1}")
            // val file = File(imagesDir, "IMG${Date().time}.png")

            if (!directory.exists())
                directory.mkdirs()

            val downloadManager =
                requireActivity().getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
            val downloadUri = Uri.parse(url)

            val request = DownloadManager.Request(downloadUri).apply {
                setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or
                            DownloadManager.Request.NETWORK_MOBILE
                )
                    .setAllowedOverRoaming(false)
                    .setTitle(url?.substring(url.lastIndexOf("/") + 1))
                    .setDescription("")
                    .setDestinationInExternalFilesDir(
                        requireActivity(),
                        directory.toString(),
                        fileName
                    )
                /// test is just replicate the above
//                .setDestinationInExternalPublicDir(
//                    directory.toString(),
//                    fileName
//                    // url?.substring(url.lastIndexOf("/")+ 1)
//                )
            }

            val downloadId = downloadManager.enqueue(request)
            val query = DownloadManager.Query().setFilterById(downloadId)

            CoroutineScope(Dispatchers.IO).launch {
                var downloading = true
                while (downloading) {
                    val cursor: Cursor = downloadManager.query(query)
                    cursor.moveToFirst()
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                        // viewModel.updateGroupIcon(iconUri)
                    }
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    msg = statusMessage(url!!, directory, status) // not null asserting url
                    if (msg != lastMsg) {
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                        }
                        lastMsg = msg ?: ""
                    }
                    cursor.close()
                }
                if (args.groupId != -1) {
                    if (args.fromGroupsSearchFragment) {
                        // Actually it has to go to group search fragment, but this update is not updating in groups fragment, for that I
                        // need to refresh layout everytime
                        viewModel.updateGroupIcon(testUri) {
                            gotoGroupsSearchFragment()
                        }
//                        viewModel.updateGroupIcon(testUri){
//                            gotoGroupsFragment()
//                        }
                    } else {
                        if (args.fromGroupsFragment)
                            viewModel.updateGroupIcon(testUri) {
                                gotoGroupsFragment()
                            }
                        else{ // means it is from create edit group, don't update group icon here, similarly do this for camera & gallery
                            CoroutineScope(Dispatchers.Main).launch {
                                gotoCreateEditGroupFragment(testUri)
                            }
//                            viewModel.updateGroupIcon(testUri) {
//                                //gotoGroupsFragment()
//                                gotoCreateEditGroupFragment()
//                            }
                        }

                    }
                } else {
                    Log.d(TAG, "downloadImage: error causing ${testUri}")
                    CoroutineScope(Dispatchers.Main).launch {
                        gotoCreateEditGroupFragment(testUri)
                    }
                }


                //goto group icon
                //gotoGroupIconFragment()
            }

//        Thread {
//            var downloading = true
//            while (downloading) {
//                val cursor: Cursor = downloadManager.query(query)
//                cursor.moveToFirst()
//                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
//                    downloading = false
//                    // viewModel.updateGroupIcon(iconUri)
//                }
//                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
//                msg = statusMessage(url!!, directory, status) // not null asserting url
//                if (msg != lastMsg) {
//                    requireActivity().runOnUiThread {
//                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
//                    }
//                    lastMsg = msg ?: ""
//                }
//                cursor.close()
//            }
//            viewModel.updateGroupIcon(testUri)
//        }.start()

        }
        else{
            downloadLater = true
            Log.d(TAG, "downloadImage: has image not loaded, but done clicked")
        }
    }

    private fun gotoGroupsFragment() {
        val action = SetImageFragmentDirections.actionSetImageFragmentToGroupsFragment()
        view?.findNavController()?.navigate(action)
    }

    private fun gotoCreateEditGroupFragment() {
        val action = SetImageFragmentDirections.actionSetImageFragmentToCreateEditGroupFragment2(
            args.groupId,
            null,
            args.groupName,
            null
        )
        view?.findNavController()?.navigate(action)
    }

    private fun gotoCreateEditGroupFragment(uri: Uri) {
        val action = SetImageFragmentDirections.actionSetImageFragmentToCreateEditGroupFragment2(
            args.groupId,
            null,
            args.groupName,
            uri.toString()
        )
        view?.findNavController()?.navigate(action)
    }

    private fun gotoGroupsSearchFragment(){
        val action = SetImageFragmentDirections.actionSetImageFragmentToSearchGroupFragment()
        view?.findNavController()?.navigate(action)
    }

    private fun statusMessage(url: String, directory: File, status: Int): String {
        var msg = ""
        msg = when (status) {
            DownloadManager.STATUS_FAILED -> getString(R.string.download_failed)
            DownloadManager.STATUS_PAUSED -> getString(R.string.paused)
            DownloadManager.STATUS_PENDING -> getString(R.string.pending)
            DownloadManager.STATUS_RUNNING -> getString(R.string.downloading)
            DownloadManager.STATUS_SUCCESSFUL -> getString(R.string.group_icon_set) //Image downloaded successfully in $directory" + File.separator + url.substring(
//                url.lastIndexOf("/") + 1
//            )
            else -> getString(R.string.nothing_to_download)
        }
        return msg
    }
}