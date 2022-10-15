package com.example.splitwise.ui.fragment.expensebills

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentExpenseBillsBinding
import com.example.splitwise.ui.fragment.adapter.BillsAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import java.io.IOException
import java.util.*

class ExpenseBillsFragment : Fragment() {
    private lateinit var binding: FragmentExpenseBillsBinding
    private lateinit var bitmap: Bitmap

    private val viewModel: ExpenseBillsViewModel by viewModels {
        ExpenseBillsViewModelFactory(requireContext(), args.expenseId)
    }

    private val args: ExpenseBillsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.bills)

        return inflater.inflate(R.layout.fragment_expense_bills, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExpenseBillsBinding.bind(view)

        viewModel.fetchBills()

        val billsAdapter = BillsAdapter{ position: Int, billImageView: View ->
            gotoBillsHolderFragment(position, billImageView)
        }

        val spanCount = if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 6
        binding.expenseBillsRecyclerView.apply {
            layoutManager = GridLayoutManager(
                requireContext(), spanCount)
            adapter = billsAdapter
        }

        // bills
        viewModel.bills.observe(viewLifecycleOwner) { bills ->
            if (bills != null && bills.isNotEmpty()) {
                billsAdapter.updateBills(bills)
                binding.expenseBillsRecyclerView.visibility = View.VISIBLE
                binding.noBillsTextView.visibility = View.GONE
            } else {
                binding.expenseBillsRecyclerView.visibility = View.GONE
                binding.noBillsTextView.visibility = View.VISIBLE
            }
        }

        // Option menu
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.expense_bills_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_bill_menu -> {
                openBottomSheet()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
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

    private fun settingsDialog() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage(getString(R.string.storage_permission))
        builder.setPositiveButton(getString(R.string.settings)) { dialog, which ->
            gotoSettings()
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.permission_granted),
                    Snackbar.LENGTH_SHORT
                ).show()
                openBottomSheet()
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

    private fun openBottomSheet() {
        val billBottomSheet = BottomSheetDialog(requireContext())
        billBottomSheet.setContentView(R.layout.add_bill_bottom_sheet)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            billBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val cameraImage =
            billBottomSheet.findViewById<ShapeableImageView>(R.id.camera_image_holder)
        val galleryImage =
            billBottomSheet.findViewById<ShapeableImageView>(R.id.gallery_image_holder)

        cameraImage?.setOnClickListener {
            openCamera()
            //Toast.makeText(requireContext(), "Camera Clicked", Toast.LENGTH_SHORT).show()
            billBottomSheet.dismiss()
        }

        galleryImage?.setOnClickListener {
            selectFile()
            billBottomSheet.dismiss()
            //Toast.makeText(requireContext(), "Gallery Clicked", Toast.LENGTH_SHORT).show()
        }

        billBottomSheet.show()

    }

    private fun openCamera(){
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

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            Log.d(TAG, "reached: ")
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                // Log.d(TAG, "onCreate: file path: captured image ${result.data?.toUri(Intent.URI_INTENT_SCHEME)}")
                val imageBitmap = result.data?.extras?.get("data") as Bitmap?

                // val x = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, )

                Log.d(TAG, ": uri is ${imageBitmap}")

                if (imageBitmap != null) {
                    bitmap = imageBitmap
                    //val uri = saveImage(bitmap, "${Date().time}.png")
                    val uri = storeUsingMediaStore(bitmap)
                    if (uri != null)
                        viewModel.addBills(uri)
                    else
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
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${Date().time}.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            // by default it is stored in external/pictures

            //put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
            // image stored in dcim is shown in gallery app
            // whereas image stored in pictures is not shown in gallery
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

                    viewModel.addBills(uri)

                } else
                    Snackbar.make(binding.root, getString(R.string.error), Snackbar.LENGTH_SHORT)
                        .show()
            }

        }


        private fun gotoBillsHolderFragment(position: Int, billImageView: View) {

        Log.d(TAG, "gotoBillsHolderFragment: position is $position")
        val action =
            ExpenseBillsFragmentDirections.actionExpenseBillsFragmentToBillsHolderFragment(
                viewModel.getBills().toTypedArray(), position, args.expenseId
            )

        findNavController().navigate(action)
    }
}