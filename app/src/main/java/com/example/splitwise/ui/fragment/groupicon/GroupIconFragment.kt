package com.example.splitwise.ui.fragment.groupicon

import android.content.ContentResolver
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.ConfigurationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentGroupIconBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.util.*


class GroupIconFragment : Fragment() {

    private lateinit var binding: FragmentGroupIconBinding
    private val args: GroupIconFragmentArgs by navArgs()
    private lateinit var bitmap: Bitmap

    private val viewModel: GroupIconViewModel by viewModels {
        GroupIconViewModelFactory(requireContext(), args.groupId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_icon, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGroupIconBinding.bind(view)

        val groupIcon = args.groupIcon

        if (groupIcon != null) {
            Log.d(TAG, "onViewCreated: group icon ${Uri.parse(groupIcon)}")
            binding.groupIconImageView.setImageURI(Uri.parse(groupIcon))

            binding.groupIconImageView.visibility = View.VISIBLE
            binding.emptyGroupIcon.visibility = View.GONE
        } else {
            binding.groupIconImageView.visibility = View.GONE
            binding.emptyGroupIcon.visibility = View.VISIBLE
        }

        // menu
        setHasOptionsMenu(true)
    }

    private fun openBottomSheet() {
        val groupIconBottomSheet = BottomSheetDialog(requireContext())
        groupIconBottomSheet.setContentView(R.layout.edit_group_icon)

        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            groupIconBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val webImage = groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.web_image_holder)
        val cameraImage =
            groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.camera_image_holder)
        val galleryImage =
            groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.gallery_image_holder)

        webImage?.setOnClickListener {
            if (isNetworkAvailable()) {
                gotoSearchImageFragment()
                groupIconBottomSheet.dismiss()
            } else
                Snackbar.make(binding.root, getString(R.string.internet_unavailable), Snackbar.LENGTH_SHORT).show()

        }

        cameraImage?.setOnClickListener {
            openCamera()
            //Toast.makeText(requireContext(), "Camera Clicked", Toast.LENGTH_SHORT).show()
            groupIconBottomSheet.dismiss()
        }

        galleryImage?.setOnClickListener {
            selectFile()
            groupIconBottomSheet.dismiss()
            //Toast.makeText(requireContext(), "Gallery Clicked", Toast.LENGTH_SHORT).show()
        }

        groupIconBottomSheet.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.group_icon_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_menu -> {
                openBottomSheet()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun openCamera() {

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intent)
                Log.d(TAG, "onViewCreated: reached")
            }
            else -> {
                requestPermissionLauncher.launch(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return when {
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) -> {
                val cap = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
                cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) -> {
                val networks = cm.allNetworks
                for (n in networks) {
                    val nInfo = cm.getNetworkInfo(n)
                    if (nInfo != null && nInfo.isConnected)
                        return true
                }
                false
            }
            else -> {
                val networks = cm.allNetworkInfo
                for (nInfo in networks) {
                    if (nInfo != null && nInfo.isConnected)
                        return true
                }
                false
            }
        }
    }

    private fun gotoSearchImageFragment() {
        val action = GroupIconFragmentDirections.actionGroupIconFragmentToSearchImageFragment(
            args.groupId,
            if (args.groupName == "") "random" else args.groupName,
            args.fromGroupsFragment,
            args.fromGroupsSearchFragment
        )
        view?.findNavController()?.navigate(action)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Snackbar.make(binding.root, getString(R.string.permission_granted), Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, getString(R.string.permission_denied), Snackbar.LENGTH_SHORT).show()
            }
        }


    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            Log.d(TAG, "reached: ")
            if (result.resultCode == AppCompatActivity.RESULT_OK) {

                val imageBitmap = result.data?.extras?.get("data") as Bitmap?

                if (imageBitmap != null) {
                    bitmap = imageBitmap
                    //val uri = saveImage(bitmap, "${Date().time}.png")
                    val uri = storeUsingMediaStore(bitmap)
                    if (uri != null) {
                        if (args.groupId != -1) {
                            if (args.fromGroupsFragment)
                                viewModel.updateGroupIcon(uri) {
                                    gotoGroupsFragment()
                                }
                            else {
                                viewModel.updateGroupIcon(uri)
                                requireActivity().onBackPressed()
                            }
                        }
                        else
                            gotoCreateEditGroupFragment(uri)
                    } else
                        Snackbar.make(binding.root, getString(R.string.error_adding_image), Snackbar.LENGTH_SHORT).show()
                } else
                    Snackbar.make(binding.root, "Bit map not found", Snackbar.LENGTH_SHORT).show()
            }
        }

    //On Android 10 (API level 29) and higher, the proper directory for sharing photos is the MediaStore.Images table.
    private fun storeUsingMediaStore(bitmap: Bitmap): Uri? {

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValue = ContentValues().apply {
//            val directory = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.toString())
//            //val path = directory.absolutePath
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${Date().time}.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")

        }

        var uri: Uri? = null

        try {
            uri = requireActivity().contentResolver.insert(
                collection,
                contentValue
            ) ?: throw IOException("Failed to create media store record")

            if (uri == null) {
                Log.d(TAG, "storeUsingMediaStore: null")
            }
            requireActivity().contentResolver.openOutputStream(uri)?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            } ?: throw IOException("Failed to open output stream")

        } catch (e: Exception) {
            Log.d(TAG, "storeUsingMediaStore: ${e.message}")
        }

        return uri
    }

    private fun selectFile() {
        //Intent.ACTION_PICK,
        //            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { //ACTION_OPEN_DOCUMENT // ACTION_GET_CONTENT
            type = "image/*"
            flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }

        selectFileLauncher.launch(intent)
    }

    private val selectFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {

                val uri = result.data?.data

                //As you are using ACTION_GET_CONTENT, your access to the Uri ends when the component is destroyed (unless you pass
                // the Uri to a new component). You must make a copy of the file from the Uri if you'd like to continue to access
                // it beyond the lifetime of your component or switch to ACTION_OPEN_DOCUMENT and persist permissions (possible only
                // with document Uris).

                if (uri != null) {
                    requireActivity().contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    if (args.groupId != -1) {
                        if (args.fromGroupsFragment) {
                            viewModel.updateGroupIcon(uri) {
                                gotoGroupsFragment()
                            }
                        } else {
                            viewModel.updateGroupIcon(uri)
                            requireActivity().onBackPressed()
                        }
                    }
                    else{
                        gotoCreateEditGroupFragment(uri)
                    }
                } else
                    Snackbar.make(binding.root, getString(R.string.error), Snackbar.LENGTH_SHORT).show()

            }

        }

    private fun gotoGroupsFragment() {
        val action = GroupIconFragmentDirections.actionGroupIconFragmentToGroupsFragment()
        view?.findNavController()?.navigate(action)
    }

    private fun gotoCreateEditGroupFragment(uri: Uri) {
        val action = GroupIconFragmentDirections.actionGroupIconFragmentToCreateEditGroupFragment(
            args.groupId, null, args.groupName, uri.toString()
        )
        view?.findNavController()?.navigate(action)
    }
}