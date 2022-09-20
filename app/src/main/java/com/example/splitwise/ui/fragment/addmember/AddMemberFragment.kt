package com.example.splitwise.ui.fragment.addmember

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentAddMemberBinding
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.formatNumber
import com.example.splitwise.util.nameCheck
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import java.io.IOException
import java.util.*

class AddMemberFragment() : Fragment() {

    private lateinit var binding: FragmentAddMemberBinding
    private val args: AddMemberFragmentArgs by navArgs()
    private val contactNameLiveData = MutableLiveData<String>()
    private val contactPhoneLiveData = MutableLiveData<String>()
    private var menuVisible = false

    private lateinit var bitmap: Bitmap

    private val viewModel: AddMemberViewModel by viewModels {
        AddMemberViewModelFactory(requireContext())
    }

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
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.add_member)
        return inflater.inflate(R.layout.fragment_add_member, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddMemberBinding.bind(view)

//        binding.outlinedMemberNameTextField.hint = getString(R.string.name)
//        binding.outlinedMemberPhoneTextField.hint = getString(R.string.phone_number)

        //binding.memberNameText.setText("test")
//        Handler(Looper.getMainLooper()).postDelayed({
////            binding.memberNameText.setText("")
////            binding.memberNameText.invalidate()
//            //binding.outlinedMemberNameTextField.hint = getString(R.string.name)
//        }, 100)

        //binding.memberNameText.invalidate()
        //binding.memberNameText.postInvalidate()


        // update group icon set before orientation change
        if(viewModel.memberProfile != null){
            updateProfile(viewModel.memberProfile!!)
        }

        // update profile click
        binding.memberImageView.setOnClickListener {
            openBottomSheet()
        }

        binding.memberImageHolderImage.setOnClickListener {
            openBottomSheet()
        }

        val nameEditText = binding.memberNameText
        val phoneEditText = binding.memberPhoneText

        val nameLayout = binding.outlinedMemberNameTextField

        // select contact button
        binding.selectContactButton.setOnClickListener {
            selectContact()
        }

        // live data
//        viewModel.exists.observe(viewLifecycleOwner) { exists ->
//            if (exists != null) {
//                if (exists) {
//                    Snackbar.make(
//                        binding.root,
//                        getString(R.string.member_exists_already),
//                        Snackbar.LENGTH_SHORT
//                    ).show()
//                } else {
//                    gotoCreateEditGroupFragment()
//                }
//            }
//        }

        contactNameLiveData.observe(viewLifecycleOwner) {
            if (it != "")
                nameEditText.setText(it)
            else
                Log.d(TAG, "onCreateDialog: name empty")
        }

        contactPhoneLiveData.observe(viewLifecycleOwner) {
            if (it != "")
                phoneEditText.setText(it)
            else
                Log.d(TAG, "onCreateDialog: phone empty")
        }

        // text watcher
        val nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (nameCheck(nameEditText.text?.trim().toString())) {
                    nameLayout.error = null
                    nameLayout.isErrorEnabled = false
                } else
                    nameLayout.error = getString(R.string.enter_valid_name)

                menuVisible =
                    nameCheck(nameEditText.text?.trim().toString()) &&
                            (phoneEditText.text?.trim().toString().length == 10)

                requireActivity().invalidateOptionsMenu()

            }

        }

        val phoneWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
//                if (phoneEditText.text?.trim().toString().length != 10)
//                    phoneLayout.error = "Enter valid phone number"
//                else {
//                    phoneLayout.error = null
//                    phoneLayout.isErrorEnabled = false
//                }
                menuVisible =
                    nameCheck(nameEditText.text?.trim().toString()) &&
                            (phoneEditText.text?.trim().toString().length == 10)

                requireActivity().invalidateOptionsMenu()

            }
        }

        nameEditText.addTextChangedListener(nameWatcher)
        phoneEditText.addTextChangedListener(phoneWatcher)

        // option menu
        setHasOptionsMenu(true)


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_member_fragment_menu, menu)

        val doneMenu = menu.findItem(R.id.add_member_save)

        doneMenu.isVisible = menuVisible
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_member_save -> {
                viewModel.checkMember(
                    args.groupId,
                    Member(
                        binding.memberNameText.text?.trim().toString(),
                        binding.memberPhoneText.text?.trim().toString().toLong(),
                        viewModel.memberProfile
                    ),
                    { member: Member ->
                        gotoCreateEditGroupFragment(member)
                    },
                    {
                        memberExists()
                    },
                    {
                        errorAdding()
                    }
                )
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    private fun errorAdding() {
        Snackbar.make(
            binding.root,
            getString(R.string.error),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun openBottomSheet() {
        val groupIconBottomSheet = BottomSheetDialog(requireContext())
        groupIconBottomSheet.setContentView(R.layout.edit_member_icon)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            groupIconBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val cameraImage =
            groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.camera_image_holder)
        val galleryImage =
            groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.gallery_image_holder)

        cameraImage?.setOnClickListener {
            openCamera()
            groupIconBottomSheet.dismiss()
        }

        galleryImage?.setOnClickListener {
            selectFile()
            groupIconBottomSheet.dismiss()
        }

        groupIconBottomSheet.show()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->


            if (isGranted) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.permission_granted),
                    Snackbar.LENGTH_SHORT
                ).show()
                openCamera() // to open it after permission grant
            } else {
                gotoSettings()
//                Snackbar.make(
//                    binding.root,
//                    getString(R.string.permission_denied),
//                    Snackbar.LENGTH_SHORT
//                ).show()
            }


        }

    private fun gotoSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
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
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
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
        builder.setPositiveButton(getString(R.string.settings)){ dialog, which ->
            gotoSettings()
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }

    private fun requestDialog() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage(getString(R.string.storage_permission))
        builder.setPositiveButton(getString(R.string.proceed)){ dialog, which ->
            //requestPermissionLauncher.unregister()
            requestPermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
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
                        viewModel.memberProfile = uri
                        updateProfile(uri)
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

    private fun updateProfile(uri: Uri) {
        ///binding.memberImageView.setImageURI(uri)
        binding.memberImageView.setImageBitmap(decodeSampledBitmapFromUri(
            binding.root.context, uri, 160.dpToPx(resources.displayMetrics), 160.dpToPx(resources.displayMetrics)
        ))

        binding.memberImageView.visibility = View.VISIBLE
        binding.memberImageHolder.visibility = View.GONE
        binding.memberImageHolderImage.visibility = View.GONE
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

                    viewModel.memberProfile = uri
                    updateProfile(uri)
                } else
                    Snackbar.make(binding.root, getString(R.string.error), Snackbar.LENGTH_SHORT)
                        .show()

            }

        }

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

    private fun gotoCreateEditGroupFragment(member: Member) {
        val action = AddMemberFragmentDirections.actionAddMemberFragmentToCreateEditGroupFragment(
            args.groupId,
            arrayOf(
                member
            ),
            args.groupName,
            args.groupIcon
        )

        view?.findNavController()?.navigate(action)
    }

    private fun memberExists(){
        Snackbar.make(
            binding.root,
            getString(R.string.member_exists_already),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun selectContact() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        Log.d(TAG, "selectContact: select contact empty")
        selectContactLauncher.launch(intent)
    }

    private val selectContactLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val contactUri: Uri? = result.data?.data

                Log.d(TAG, ": inside launcher empty")
                val projections = listOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ).toTypedArray()

                val cursor = requireContext().contentResolver.query(
                    contactUri!!,
                    projections,
                    null,
                    null,
                    null
                )

                if (cursor != null) {
                    cursor.moveToFirst()
                    val numberColumnIndex =
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val number = cursor.getString(numberColumnIndex).formatNumber()
                    contactPhoneLiveData.value = number

                    Log.d(TAG, "$number: empty")
                    val nameColumnIndex =
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val name = cursor.getString(nameColumnIndex)

                    contactNameLiveData.value = name

                    Log.d(TAG, "$name: empty")
                    cursor.close()

                } else
                    Toast.makeText(requireContext(), "Error selecting contact", Toast.LENGTH_LONG)
                        .show()
            } else
                Log.d(TAG, "result not ok: empty")

        }

}