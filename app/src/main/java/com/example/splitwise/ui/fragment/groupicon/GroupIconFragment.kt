package com.example.splitwise.ui.fragment.groupicon

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
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
                Toast.makeText(requireContext(), getString(R.string.internet_unavailable), Toast.LENGTH_SHORT).show()

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
            args.groupName
        )
        view?.findNavController()?.navigate(action)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(requireContext(), getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
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
                    if (uri != null)
                        viewModel.updateGroupIcon(uri) {
                            gotoGroupsFragment()
                        }
                    else
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_adding_image),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                } else
                    Toast.makeText(requireContext(), "Bit map not found", Toast.LENGTH_SHORT)
                        .show()
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
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }

        selectFileLauncher.launch(intent)
    }

    private val selectFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {

                val uri = result.data?.data

                if (uri != null) {
                    viewModel.updateGroupIcon(uri) {
                        gotoGroupsFragment()
                    }
                } else
                    Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()


            }

        }

    private fun gotoGroupsFragment() {
        val action = GroupIconFragmentDirections.actionGroupIconFragmentToGroupsFragment()
        view?.findNavController()?.navigate(action)
    }
}