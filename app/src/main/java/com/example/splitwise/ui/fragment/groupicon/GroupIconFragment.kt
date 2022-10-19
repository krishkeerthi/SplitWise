package com.example.splitwise.ui.fragment.groupicon

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.ConfigurationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentGroupIconBinding
import com.example.splitwise.util.playDeleteSound
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_container
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = resources.getColor(R.color.background)//Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.background))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.group_icon)
        return inflater.inflate(R.layout.fragment_group_icon, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGroupIconBinding.bind(view)

        val groupIcon = args.groupIcon

        if (groupIcon != null) {
            Log.d(TAG, "onViewCreated: group icon ${Uri.parse(groupIcon)}")
            ///binding.groupIconImageView.setImageURI(Uri.parse(groupIcon))
            binding.groupIconImageView.setImageURI(Uri.parse(groupIcon))
            binding.groupIconImageView.visibility = View.VISIBLE
            binding.emptyGroupIcon.visibility = View.GONE
           // binding.uploadGroupIconButton.visibility = View.GONE
        } else {
            binding.groupIconImageView.visibility = View.GONE
            binding.emptyGroupIcon.visibility = View.VISIBLE
           // binding.uploadGroupIconButton.visibility = View.VISIBLE
        }

//        binding.uploadGroupIconButton.setOnClickListener{
//            openBottomSheet()
//        }

        // edit mode
        if(args.edit){
            openBottomSheet()
        }

        // menu
        setHasOptionsMenu(true)
    }

    private fun openBottomSheet() {
        val groupIconBottomSheet = BottomSheetDialog(requireContext())
        groupIconBottomSheet.setContentView(R.layout.edit_group_icon)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            groupIconBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val webImage = groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.web_image_holder)
        val cameraImage =
            groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.camera_image_holder)
        val galleryImage =
            groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.gallery_image_holder)
        val deleteImage = groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.delete_image_holder)
        val deleteIcon = groupIconBottomSheet.findViewById<AppCompatImageView>(R.id.delete_icon)

        if(args.groupIcon == null) {
            deleteImage?.visibility = View.GONE
            deleteIcon?.visibility = View.GONE
        }

        // delete icon
       deleteImage?.setOnClickListener {
           if(args.groupIcon != null){
               // later ref
               val alertDialog = AlertDialog.Builder(requireContext())
               alertDialog.setMessage(getString(R.string.delete_message))

               alertDialog.setPositiveButton(getString(R.string.delete)){ dialog, which ->
                   if (args.groupId != -1) {
                       if (args.fromGroupsSearchFragment) {
                           viewModel.removeGroupIcon {
                               gotoSearchGroupsFragment()
                           }
                       } else {
                           if (args.fromGroupsFragment)
                               viewModel.removeGroupIcon {
                                   gotoGroupsFragment()
                               }
                           else { // from create edit group,(during edit group)
                               gotoCreateEditGroupFragment(null)
//                                    viewModel.updateGroupIcon(uri)
//                                    NavHostFragment.findNavController(this).popBackStack()
                               //requireActivity().onBackPressed()  for back press I used this, this is wrong
                           }
                       }
                   } else // without groupid, during creation only we can be here
                       gotoCreateEditGroupFragment(null)

                   playDeleteSound(requireContext())
                   //Toast.makeText(requireContext(), getString(R.string.group_icon_deleted), Toast.LENGTH_SHORT).show()
               }
               alertDialog.setNegativeButton(getString(R.string.cancel), null)
               alertDialog.show()
           }
           else{
               Toast.makeText(requireContext(), getString(R.string.no_group_icon), Toast.LENGTH_SHORT).show()
           }

           groupIconBottomSheet.dismiss()
       }


        // web icon
        webImage?.setOnClickListener {
            if (isNetworkAvailable()) {
                gotoSearchImageFragment()
                groupIconBottomSheet.dismiss()
            } else {
//                Snackbar.make(
//                    binding.root,
//                    getString(R.string.internet_unavailable),
//                    Snackbar.LENGTH_SHORT
//                ).show()

                Toast.makeText(requireContext(), getString(R.string.internet_unavailable), Toast.LENGTH_SHORT).show()
            }

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

//        val editMenu = menu.findItem(R.id.edit_menu)
//
//        editMenu.isVisible = args.groupIcon != null

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // on sdk 33 onwards there is no write external storage permission, thus camera was not loading
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        } else {
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
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        settingsDialog()
                    } else {
                        requestDialog()
                    }
                }
            }
        }
    }

    private fun settingsDialog() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage(getString(R.string.storage_permission))
        builder.setPositiveButton(getString(R.string.settings)) { dialog, which ->
            gotoSettings()
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }

    private fun requestDialog() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage(getString(R.string.storage_permission))
        builder.setPositiveButton(getString(R.string.proceed)) { dialog, which ->
            //requestPermissionLauncher.unregister()
            requestPermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
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
            args.groupName, //if (args.groupName == "") "random" else args.groupName
            args.fromGroupsFragment,
            args.fromGroupsSearchFragment
        )
        view?.findNavController()?.navigate(action)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.permission_granted),
                    Snackbar.LENGTH_SHORT
                ).show()
                openCamera()
            } else {
                gotoSettings()
                //Snackbar.make(binding.root, getString(R.string.permission_denied), Snackbar.LENGTH_SHORT).show()
            }
        }

    private fun gotoSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
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
                            if (args.fromGroupsSearchFragment) {
                                viewModel.updateGroupIcon(uri) {
                                    gotoSearchGroupsFragment()
                                }
                            } else {
                                if (args.fromGroupsFragment)
                                    viewModel.updateGroupIcon(uri) {
                                        gotoGroupsFragment()
                                    }
                                else { // from create edit group,(during edit group)
                                    gotoCreateEditGroupFragment(uri)
//                                    viewModel.updateGroupIcon(uri)
//                                    NavHostFragment.findNavController(this).popBackStack()
                                    //requireActivity().onBackPressed()  for back press I used this, this is wrong
                                }
                            }
                        } else // without groupid, during creation only we can be here
                            gotoCreateEditGroupFragment(uri)
                    } else
                        Snackbar.make(
                            binding.root,
                            getString(R.string.error_adding_image),
                            Snackbar.LENGTH_SHORT
                        ).show()
                } else
                    Snackbar.make(
                        binding.root,
                        getString(R.string.bitmap_not_found),
                        Snackbar.LENGTH_SHORT
                    ).show()
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
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply { //ACTION_OPEN_DOCUMENT // ACTION_GET_CONTENT
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
                    requireActivity().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    if (args.groupId != -1) {
                        if (args.fromGroupsSearchFragment) {
                            viewModel.updateGroupIcon(uri) {
                                gotoSearchGroupsFragment()
                            }
                        } else {
                            if (args.fromGroupsFragment) {
                                viewModel.updateGroupIcon(uri) {
                                    gotoGroupsFragment()
                                }
                            } else {
                                gotoCreateEditGroupFragment(uri)
//                                viewModel.updateGroupIcon(uri)
//                                NavHostFragment.findNavController(this).popBackStack()
                                //requireActivity().onBackPressed() for back press I used this, this is wrong
                            }
                        }
                    } else {
                        gotoCreateEditGroupFragment(uri)
                    }
                } else
                    Snackbar.make(binding.root, getString(R.string.error), Snackbar.LENGTH_SHORT)
                        .show()

            }

        }

    private fun gotoGroupsFragment() {
        val action = GroupIconFragmentDirections.actionGroupIconFragmentToGroupsFragment()
        view?.findNavController()?.navigate(action)
    }

    private fun gotoSearchGroupsFragment() {
        val action = GroupIconFragmentDirections.actionGroupIconFragmentToSearchGroupFragment()
        view?.findNavController()?.navigate(action)
    }

    private fun gotoCreateEditGroupFragment(uri: Uri?) {
        val action = GroupIconFragmentDirections.actionGroupIconFragmentToCreateEditGroupFragment(
            args.groupId, null, args.groupName, uri?.toString()
        )
        view?.findNavController()?.navigate(action)
    }

}