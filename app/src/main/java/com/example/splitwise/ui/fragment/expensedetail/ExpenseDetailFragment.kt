package com.example.splitwise.ui.fragment.expensedetail

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentExpenseDetailBinding
import com.example.splitwise.ui.fragment.adapter.ExpenseMembersAdapter
import com.example.splitwise.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_container
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = resources.getColor(R.color.view_color)//Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.background))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.expense_detail)
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

        // start enter transition only when data loaded, and just started to draw (for bills)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        //

        binding = FragmentExpenseDetailBinding.bind(view)

        //requireActivity().title = "Expense Detail"

        // need to fetch here otherwise after deleting bills updated list won't show
        viewModel.fetchBills()

        // Rv
        val membersAdapter = ExpenseMembersAdapter()
//        val billsAdapter = BillsAdapter { position: Int, billImageView: View ->
//            //gotoBillsHolderFragment(position, billImageView)
//        }

        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }

//        binding.billsRecyclerView.apply {
//            layoutManager = LinearLayoutManager(
//                requireContext(), LinearLayoutManager.HORIZONTAL,
//                false
//            )
//            adapter = billsAdapter
//        }

        // Livedata payees
        viewModel.payees.observe(viewLifecycleOwner) { members ->
            if (members != null) {
                membersAdapter.updateMembers(members)
            }
        }

        viewModel.removedPayees.observe(viewLifecycleOwner) { removedMembers ->
            if (removedMembers != null && removedMembers.isNotEmpty()) {
                binding.removedMembersTextView.text =
                    "${getString(R.string.removed_members)} (${removedMembers.size})"
                binding.removedMembersTextView.visibility = View.VISIBLE
            } else {
                binding.removedMembersTextView.visibility = View.GONE
            }
        }
        // bills
//        viewModel.bills.observe(viewLifecycleOwner) { bills ->
//            if (bills != null && bills.isNotEmpty()) {
//                billsAdapter.updateBills(bills)
//                binding.billsRecyclerView.visibility = View.VISIBLE
//                binding.billsTextView.visibility = View.VISIBLE
//            } else {
//                binding.billsRecyclerView.visibility = View.GONE
//                binding.billsTextView.visibility = View.INVISIBLE
//            }
//        }

        // Expense
        viewModel.expense.observe(viewLifecycleOwner) { expense ->
            expense?.let {
                //requireActivity().title = it.expenseName
                binding.groupExpenseTitleTextView.text = it.expenseName
                //membersAdapter.updateTotal(it.totalAmount) previously total amount is passed and divided by member count
                membersAdapter.updateTotal(it.splitAmount)
                binding.expenseTotalTextView.text = "₹" + it.totalAmount.roundOff()
                binding.expenseImageView.setImageResource(getCategoryDrawableResource(Category.values()[it.category].ordinal))
                binding.expenseCategoryTextView.text =
                    Category.values()[it.category].name.lowercase().titleCase()
            }
        }

        // Payer
        viewModel.payer.observe(viewLifecycleOwner) { payer ->
            payer?.let {
                binding.expensePayerTextView.text = it.name

                if (it.memberProfile != null) {
                    binding.memberImageView.setImageBitmap(
                        getRoundedCroppedBitmap(
                        decodeSampledBitmapFromUri(
                            binding.root.context,
                            it.memberProfile,
                            40.dpToPx(resources.displayMetrics),
                            40.dpToPx(resources.displayMetrics)
                        )!!
                        )
                    )
                    binding.memberImageView.visibility = View.VISIBLE
                    binding.memberImageHolder.visibility = View.INVISIBLE
                    binding.memberImageHolderImage.visibility = View.INVISIBLE
                }
            }
        }

        // Bills images
//        binding.addBillButton.setOnClickListener {
////            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)//.also { takePictureIntent ->
////                // Ensure that there's a camera activity to handle the intent
////                takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
////                    // Create the File where the photo should go
////                    val photoFile: File? = try {
////                        createImageFile()
////                    } catch (ex: IOException) {
////                        // Error occurred while creating the File
////                        null
////                    }
////                    // Continue only if the File was successfully created
////                    photoFile?.also {
////                        val photoURI: Uri = FileProvider.getUriForFile(
////                            requireContext(),
////                            "com.example.splitwise.fileprovider",
////                            it
////                        )
//////                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//////                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
////
////                    }
////
////                }
//            //     }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                // on sdk 33 onwards there is no write external storage permission, thus camera was not loading
//                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                cameraLauncher.launch(intent)
//            } else {
//                when (PackageManager.PERMISSION_GRANTED) {
//                    ContextCompat.checkSelfPermission(
//                        requireContext(),
//                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    ) -> {
//                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                        cameraLauncher.launch(intent)
//                        Log.d(TAG, "onViewCreated: reached")
//                    }
//
//                    else -> {
//                        if (ActivityCompat.shouldShowRequestPermissionRationale(
//                                requireActivity(),
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            )
//                        ) {
//                            settingsDialog()
//                        } else {
//                            requestDialog()
//                        }
//                    }
//                }
//            }
//        }

//        val swipeToDeleteCallback = object : SwipeToDeleteCallback() {
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//
//                viewModel.payees.value?.let {
//                    confirmationDialog(
//                        it[viewHolder.absoluteAdapterPosition],
//                        viewHolder.absoluteAdapterPosition
//                    )
//                }
//            }
//
//        }
//
//
//        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
//        itemTouchHelper.attachToRecyclerView(binding.membersRecyclerView)

        binding.removedMembersTextView.setOnClickListener {
            gotoRemovedMembersFragment()
        }

        // Options menu
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.expense_detail_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_expense_menu -> {
                gotoEditExpenseFragment()
                true
            }
            R.id.delete_expense_menu -> {
                expenseDeleteConfirmationDialog(
                    viewModel.expense.value?.groupId,
                    viewModel.expense.value?.expenseName
                )
                true
            }
            R.id.bills_menu -> {
                gotoExpenseBillsFragment()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun gotoEditExpenseFragment() {
        val action =
            ExpenseDetailFragmentDirections.actionExpenseDetailFragmentToEditExpenseFragment(
                args.groupId,
                viewModel.expense?.value!!, viewModel.payees?.value!!.toTypedArray()
            )

        findNavController().navigate(action)
    }

    private fun gotoRemovedMembersFragment() {
        viewModel.removedPayees.value?.let {
            Log.d(TAG, "gotoRemovedMembersFragment: ${it.size}")
            val action =
                ExpenseDetailFragmentDirections.actionExpenseDetailFragmentToRemovedMembersFragment(
                    it.toTypedArray()
                )
            findNavController().navigate(action)
        }
    }

    private fun expenseDeleteConfirmationDialog(groupId: Int?, expenseName: String?) {
        val dialogBuilder = AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.delete_expense))
            setMessage(String.format(getString(R.string.delete_expense_message), expenseName))

            setPositiveButton(getString(R.string.delete)) { dialog, which ->
                //Toast.makeText(requireContext(), "${member.name} deleted", Toast.LENGTH_SHORT).show()

                groupId?.let {
                    viewModel.deleteExpense(groupId, args.expenseId) {
                        gotoExpensesFragment()
                        Snackbar.make(
                            binding.root,
                            "$expenseName ${getString(R.string.deleted)}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }
        }

        dialogBuilder.show()

    }

    private fun gotoExpensesFragment() {
        val action =
            ExpenseDetailFragmentDirections.actionExpenseDetailFragmentToExpensesFragment(args.groupId)
        findNavController().navigate(action)
    }

    private fun confirmationDialog(member: Member, position: Int) {
        val dialogBuilder = AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.delete))
            setMessage(getString(R.string.delete_message))

            setPositiveButton(getString(R.string.delete)) { dialog, which ->
                //Toast.makeText(requireContext(), "${member.name} deleted", Toast.LENGTH_SHORT).show()

                viewModel.deletePayee(args.expenseId, member, {
                    viewModel.fetchMembers()
                    Snackbar.make(
                        binding.root,
                        "${member.name} ${getString(R.string.deleted)}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }, {
                    binding.membersRecyclerView.adapter?.notifyItemChanged(position)
                    Snackbar.make(
                        binding.root,
                        getString(R.string.payer_should_not_deleted),
                        Snackbar.LENGTH_SHORT
                    ).show()
                })
            }

            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                binding.membersRecyclerView.adapter?.notifyItemChanged(position)
                dialog.cancel()
            }
        }

        dialogBuilder.show()

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

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.permission_granted),
                    Snackbar.LENGTH_SHORT
                ).show()
                // binding.addBillButton.performClick() to open camera after permission granted
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

    private fun gotoExpenseBillsFragment() {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val action =
            ExpenseDetailFragmentDirections.actionExpenseDetailFragmentToExpenseBillsFragment(args.expenseId)

        findNavController().navigate(action)
    }


//    private fun gotoBillsHolderFragment(position: Int, billImageView: View) {
//
////        exitTransition = MaterialElevationScale(false).apply {
////            duration = resources.getInteger(R.integer.reply_motion_duration_small).toLong()
////        }
//        reenterTransition = MaterialElevationScale(true).apply {
//            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
//        }
//
////        val transitionName = getString(R.string.bill_holder_transition_name)
////        val extras = FragmentNavigatorExtras(billImageView to transitionName)
//
//        Log.d(TAG, "gotoBillsHolderFragment: position is $position")
//        val action =
//            ExpenseDetailFragmentDirections.actionExpenseDetailFragmentToBillsHolderFragment(
//                viewModel.getBills().toTypedArray(), position, args.expenseId
//            )
//
//        findNavController().navigate(action)//, extras)
//    }
}