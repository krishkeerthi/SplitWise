package com.example.splitwise.ui.fragment.expensedetail

import android.content.ContentResolver
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentExpenseDetailBinding
import com.example.splitwise.ui.fragment.adapter.BillsAdapter
import com.example.splitwise.ui.fragment.adapter.ExpenseMembersAdapter
import com.example.splitwise.util.Category
import com.example.splitwise.util.roundOff
import com.example.splitwise.util.titleCase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class ExpenseDetailFragment : Fragment() {

    private lateinit var binding: FragmentExpenseDetailBinding
    private val args: ExpenseDetailFragmentArgs by navArgs()
    private lateinit var bitmap: Bitmap

    private lateinit var currentPhotoPath: String

    private val viewModel: ExpenseDetailViewModel by viewModels {
        ExpenseDetailViewModelFactory(requireContext(), args.expenseId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_expense_detail, container, false)
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExpenseDetailBinding.bind(view)

        //requireActivity().title = "Expense Detail"

        // Rv
        val membersAdapter = ExpenseMembersAdapter()
        val billsAdapter = BillsAdapter { position: Int ->
            gotoBillsHolderFragment(position)
        }

        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }

        binding.billsRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = billsAdapter
        }

        // Livedata payees
        viewModel.payees.observe(viewLifecycleOwner) { members ->
            if (members != null) {
                membersAdapter.updateMembers(members)
            }
        }

        // bills
        viewModel.bills.observe(viewLifecycleOwner) { bills ->
            if (bills != null && bills.isNotEmpty()) {
                billsAdapter.updateBills(bills)
                binding.billsRecyclerView.visibility = View.VISIBLE
                binding.billsTextView.visibility = View.VISIBLE
            }
            else {
                binding.billsRecyclerView.visibility = View.GONE
                binding.billsTextView.visibility = View.INVISIBLE
            }

        }

        // Expense
        viewModel.expense.observe(viewLifecycleOwner) { expense ->
            expense?.let {
                //requireActivity().title = it.expenseName
                binding.groupExpenseTitleTextView.text = it.expenseName
                membersAdapter.updateTotal(it.totalAmount)
                binding.expenseTotalTextView.text = "₹" + it.totalAmount.roundOff()
                binding.expenseCategoryExpense.text = Category.values()[it.category].name.lowercase().titleCase()
            }
        }

        // Payer
        viewModel.payer.observe(viewLifecycleOwner) { payer ->
            payer?.let {
                binding.expensePayerTextView.text = it.name
            }
        }

        // Bills images
        binding.addBillButton.setOnClickListener {
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)//.also { takePictureIntent ->
//                // Ensure that there's a camera activity to handle the intent
//                takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
//                    // Create the File where the photo should go
//                    val photoFile: File? = try {
//                        createImageFile()
//                    } catch (ex: IOException) {
//                        // Error occurred while creating the File
//                        null
//                    }
//                    // Continue only if the File was successfully created
//                    photoFile?.also {
//                        val photoURI: Uri = FileProvider.getUriForFile(
//                            requireContext(),
//                            "com.example.splitwise.fileprovider",
//                            it
//                        )
////                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
////                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//
//                    }
//
//                }
            //     }


            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraLauncher.launch(intent)
                    Log.d(TAG, "onViewCreated: reached")
                }
                //                shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                //
                //                }
                else -> {
                    requestPermissionLauncher.launch(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }
        }

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

//                val photoFile: File? = try {
//                    createImageFile()
//                } catch (ex: IOException) {
//                    // Error occurred while creating the File
//                    Log.d(TAG, "file error: ")
//                    null
//                }
//                // Continue only if the File was successfully created
//                photoFile?.also {
//                    val photoURI: Uri = FileProvider.getUriForFile(
//                        requireContext(),
//                        "com.example.splitwise.fileprovider",
//                        it
//                    )
//
//                    viewModel.addBills(photoURI)
//                }

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

    // @Throws(IOException::class)
    private fun saveImage(bitmap: Bitmap, name: String): Uri? {
        var uri: Uri?
        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver: ContentResolver = requireActivity().contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            //   contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$IMAGES_FOLDER_NAME")

            val collection = MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )

            uri = resolver.insert(collection, contentValues)
            fos = resolver.openOutputStream(uri!!)
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ).toString() + File.separator //+ IMAGES_FOLDER_NAME
            val file = File(imagesDir)
            uri = file.toUri()
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, "$name.png")
            fos = FileOutputStream(image)

        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos!!)
        fos.flush()
        fos.close()

        return uri
    }


    private fun gotoBillsHolderFragment(position: Int) {
        Log.d(TAG, "gotoBillsHolderFragment: position is $position")
        val action =
            ExpenseDetailFragmentDirections.actionExpenseDetailFragmentToBillsHolderFragment(
                viewModel.getBills().toTypedArray(), position
            )

        view?.findNavController()?.navigate(action)
    }
}