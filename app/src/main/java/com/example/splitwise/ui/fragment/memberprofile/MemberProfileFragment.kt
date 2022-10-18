package com.example.splitwise.ui.fragment.memberprofile

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentMemberProfileBinding
import com.example.splitwise.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import java.io.IOException
import java.util.*

class MemberProfileFragment : Fragment() {

    private lateinit var binding: FragmentMemberProfileBinding
    private val args: MemberProfileFragmentArgs by navArgs()
    private val viewModel: MemberProfileViewModel by viewModels {
        MemberProfileViewModelFactory(requireContext(), args.memberId)
    }

    private lateinit var bitmap: Bitmap
    private var saveMenuVisible = false
    private var changeMade = false

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
                if (checkForChanges()) {
                    val builder = AlertDialog.Builder(requireContext())

                    builder.setTitle(getString(R.string.discard))
                    builder.setMessage(getString(R.string.discard_changes))
                    builder.setPositiveButton(
                        getString(R.string.discard)
                    ) { dialog, which ->

                        NavHostFragment.findNavController(this@MemberProfileFragment)
                            .popBackStack()
                    }
                    builder.setNegativeButton(getString(R.string.cancel), null)
                    builder.show()
                } else {
                    NavHostFragment.findNavController(this@MemberProfileFragment)
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
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.member_profile)
        return inflater.inflate(R.layout.fragment_member_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMemberProfileBinding.bind(view)

        viewModel.fetchData()

        // set edit mode
        if (viewModel.editEnabled) {
            enableEdit()
        } else {
            disableEdit()
        }


        if(viewModel.updatedUri != null) {
            showProfile(viewModel.updatedUri!!)
        }
        // image click
        binding.memberImageHolder.setOnClickListener {
            if (viewModel.editEnabled)
                openBottomSheet()
        }

        binding.memberImageView.setOnClickListener {
            if (viewModel.editEnabled)
                openBottomSheet()
        }


        viewModel.member.observe(viewLifecycleOwner) { member ->
            if (member != null)
                updateUI(member)
        }

        // options menu
        setHasOptionsMenu(true)


        // text watcher

        val nameEditText = binding.memberNameText
        val phoneEditText = binding.memberPhoneText

        val nameLayout = binding.outlinedMemberNameTextField
        val phoneLayout = binding.outlinedMemberPhoneTextField

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

                changeMade = true
                saveMenuVisible =
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
                if (phoneEditText.text?.trim().toString().length != 10)
                    phoneLayout.error = "Enter valid phone number"
                else {
                    phoneLayout.error = null
                    phoneLayout.isErrorEnabled = false
                }
                changeMade = true
                saveMenuVisible =
                    nameCheck(nameEditText.text?.trim().toString()) &&
                            (phoneEditText.text?.trim().toString().length == 10)

                requireActivity().invalidateOptionsMenu()

            }
        }

        nameEditText.addTextChangedListener(nameWatcher)
        phoneEditText.addTextChangedListener(phoneWatcher)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.member_profile_fragment_menu, menu)

        val editMenu = menu.findItem(R.id.edit_member)
        val saveMenu = menu.findItem(R.id.update_member)

        editMenu.isVisible = !viewModel.editEnabled
        saveMenu.isVisible = viewModel.editEnabled //&& saveMenuVisible && changeMade

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_member -> {
                viewModel.editEnabled = true
                // to enable edit options menu
                requireActivity().invalidateOptionsMenu()

                Snackbar.make(binding.root, getString(R.string.edit_member), Snackbar.LENGTH_SHORT)
                    .show()
                enableEdit()
                true
            }
            R.id.update_member -> {
                updateProfile()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    private fun updateProfile() {
        if (binding.memberNameText.text != null && binding.memberNameText.text!!.trim()
                .toString() != "" && binding.memberPhoneText.text != null && binding.memberPhoneText.text!!.trim()
                .toString() != "" && binding.memberPhoneText.text!!.trim().toString().length == 10
        ) {
            if (!nameCheck(binding.memberNameText.text!!.trim().toString())) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.invalid_input),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                val name = binding.memberNameText.text?.trim().toString()
                val phone = binding.memberPhoneText.text?.trim().toString()

                if ((viewModel.member.value!!.name != name) ||
                    (viewModel.member.value!!.phone.toString() != phone)
                    || (viewModel.member.value!!.memberProfile != viewModel.updatedUri)
                ) {
                    if(numberCheck(binding.memberPhoneText.text?.trim().toString().toLong())){
                        // new Update group
                        val builder = AlertDialog.Builder(requireContext())

                        builder.setMessage(getString(R.string.confirm_editing_member))

                        builder.setPositiveButton(getString(R.string.confirm)) { dialog, which ->

                            viewModel.newUpdateMember(
                                if(args.memberId != 1) name else "$name(You)",
                                phone,
                                {
                                    gotoCreateEditGroupFragment()
                                }, {
                                    Snackbar.make(
                                        binding.root,
                                        getString(R.string.member_updated),
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                })

                        }

                        builder.setNegativeButton(getString(R.string.cancel), null)

                        builder.show()
                    }
                    else{
                        Snackbar.make(
                            binding.root,
                            getString(R.string.number_starts_with_0),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    NavHostFragment.findNavController(this@MemberProfileFragment)
                        .popBackStack()
//                    Snackbar.make(
//                        binding.root,
//                        getString(R.string.fields_not_edited),
//                        Snackbar.LENGTH_SHORT
//                    ).show()
                }
            }
        } else {
            Snackbar.make(
                binding.root,
                getString(R.string.field_empty),
                Snackbar.LENGTH_SHORT
            ).show()
        }
//
//        viewModel.newUpdateMember()
//        viewModel.updateMember(
//            binding.memberNameText.text?.trim().toString(),
//            binding.memberPhoneText.text?.trim().toString().toLong()
//        ) {
//            gotoCreateEditGroupFragment()
//        }
    }

    private fun gotoCreateEditGroupFragment() {
        Snackbar.make(binding.root, getString(R.string.member_updated), Snackbar.LENGTH_SHORT)
            .show()
        val action =
            MemberProfileFragmentDirections.actionMemberProfileFragmentToCreateEditGroupFragment(
                args.groupId, null, args.groupName, args.groupIcon
            )
        view?.findNavController()?.navigate(action)
    }

    private fun updateUI(member: Member) {

        if (!viewModel.editEnabled) {
            // it ensures edited value won't change after orientation change when edit enabled
            binding.dummyMemberNameText.setText(member.name)
            binding.dummyMemberPhoneText.setText(member.phone.toString())

            if(member.memberId == 1) {
                binding.memberNameText.setText(member.name.substring(0, member.name.length - 5))
            }
            else{
                binding.memberNameText.setText(member.name)
            }
            binding.memberPhoneText.setText(member.phone.toString())

//            Log.d(
//                TAG,
//                "onViewCreated: ${
//                    binding.memberNameText.text?.trim().toString()
//                },${binding.memberPhoneText.text?.trim().toString().toLong()}"
//            )

        }

        viewModel.updatedUri = member.memberProfile

        // update profile irrespective of edit enabled
        if (member.memberProfile != null) {
            // member profile is stored
            ///binding.memberImageView.setImageURI(member.memberProfile)
//            binding.memberImageView.setImageBitmap(
//                getRoundedCroppedBitmap(
//                    decodeSampledBitmapFromUri(
//                        binding.root.context,
//                        member.memberProfile,
//                        160.dpToPx(resources.displayMetrics),
//                        160.dpToPx(resources.displayMetrics)
//                    )!!
//                )
//            )
            showProfile(member.memberProfile!!)
            setImageVisibility()
        }
    }

    private fun showProfile(uri: Uri){
        binding.memberImageView.setImageBitmap(
            getRoundedCroppedBitmap(
                decodeSampledBitmapFromUri(
                    binding.root.context,
                    uri,
                    160.dpToPx(resources.displayMetrics),
                    160.dpToPx(resources.displayMetrics)
                )!!
            )
        )
    }
    private fun setImageVisibility() {
        binding.memberImageView.visibility = View.VISIBLE
        binding.memberImageHolder.visibility = View.GONE
        binding.memberImageHolderImage.visibility = View.GONE
    }

    private fun enableEdit() {
////        binding.memberNameText.isEnabled = true
//        binding.memberNameText.isClickable = true
//        binding.memberNameText.isFocusable = true
//        binding.memberNameText.invalidate()
//
////        binding.memberPhoneText.isEnabled = true
//        binding.memberPhoneText.isClickable = true
//        binding.memberPhoneText.isFocusable = true
//        binding.memberPhoneText.invalidate()

        binding.dummyMemberNameText.visibility = View.INVISIBLE
        binding.dummyOutlinedMemberNameTextField.visibility = View.INVISIBLE
        binding.dummyMemberPhoneText.visibility = View.INVISIBLE
        binding.dummyOutlinedMemberPhoneTextField.visibility = View.INVISIBLE

        binding.memberNameText.visibility = View.VISIBLE
        binding.outlinedMemberNameTextField.visibility = View.VISIBLE
        binding.memberPhoneText.visibility = View.VISIBLE
        binding.outlinedMemberPhoneTextField.visibility = View.VISIBLE
    }

    private fun disableEdit() {
//        binding.memberNameText.isEnabled = false
//
//        binding.memberNameText.isClickable = false
//        binding.memberNameText.isFocusable = false
//        binding.memberNameText.invalidate()
//
////        binding.memberPhoneText.isEnabled = false
//        binding.memberPhoneText.isClickable = false
//        binding.memberPhoneText.isFocusable = false
//        binding.memberPhoneText.invalidate()

        binding.dummyMemberNameText.visibility = View.VISIBLE
        binding.dummyOutlinedMemberNameTextField.visibility = View.VISIBLE
        binding.dummyMemberPhoneText.visibility = View.VISIBLE
        binding.dummyOutlinedMemberPhoneTextField.visibility = View.VISIBLE

        binding.memberNameText.visibility = View.INVISIBLE
        binding.outlinedMemberNameTextField.visibility = View.INVISIBLE
        binding.memberPhoneText.visibility = View.INVISIBLE
        binding.outlinedMemberPhoneTextField.visibility = View.INVISIBLE
    }

    // Needs to remove, because it is repeating

    private fun openBottomSheet() {
        val groupIconBottomSheet = BottomSheetDialog(requireContext())
        groupIconBottomSheet.setContentView(R.layout.edit_member_icon)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            groupIconBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val cameraImage =
            groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.camera_image_holder)
        val galleryImage =
            groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.gallery_image_holder)
        val deleteImage =
            groupIconBottomSheet.findViewById<ShapeableImageView>(R.id.delete_image_holder)
        val deleteIcon = groupIconBottomSheet.findViewById<AppCompatImageView>(R.id.delete_image)

        viewModel.member.value?.let {
            if(it.memberProfile == null){
                deleteImage?.visibility = View.GONE
                deleteIcon?.visibility = View.GONE
            }
        }

        cameraImage?.setOnClickListener {
            openCamera()
            groupIconBottomSheet.dismiss()
        }

        galleryImage?.setOnClickListener {
            selectFile()
            groupIconBottomSheet.dismiss()
        }

        deleteImage?.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setMessage(getString(R.string.delete_message))
            alertDialog.setPositiveButton(R.string.delete){ dialog, which ->
                updateProfile(null)
            }
            alertDialog.setNegativeButton(getString(R.string.cancel), null)
            alertDialog.show()

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
                openCamera()
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
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraLauncher.launch(intent)
                    Log.d(ContentValues.TAG, "onViewCreated: reached")
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

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            Log.d(ContentValues.TAG, "reached: ")
            if (result.resultCode == AppCompatActivity.RESULT_OK) {

                val imageBitmap = result.data?.extras?.get("data") as Bitmap?

                if (imageBitmap != null) {
                    bitmap = imageBitmap
                    //val uri = saveImage(bitmap, "${Date().time}.png")
                    val uri = storeUsingMediaStore(bitmap)
                    if (uri != null) {

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
                Log.d(ContentValues.TAG, "storeUsingMediaStore: null")
            }
            requireActivity().contentResolver.openOutputStream(uri)?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            } ?: throw IOException("Failed to open output stream")

        } catch (e: Exception) {
            Log.d(ContentValues.TAG, "storeUsingMediaStore: ${e.message}")
        }

        return uri
    }

    private fun updateProfile(uri: Uri?) {
//        Snackbar.make(binding.root, getString(R.string.profile_updated), Snackbar.LENGTH_SHORT)
//            .show()

        // previously I directly updated uri, but now I am saving in viewmodel and updated later, later ref
        //viewModel.updateProfile(uri)
        viewModel.updatedUri = uri

        if(uri != null){
            ///binding.memberImageView.setImageURI(uri)
            binding.memberImageView.setImageBitmap(
                getRoundedCroppedBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context,
                        uri,
                        160.dpToPx(resources.displayMetrics),
                        160.dpToPx(resources.displayMetrics)
                    )!!
                )
            )

            binding.memberImageView.visibility = View.VISIBLE
            binding.memberImageHolder.visibility = View.GONE
            binding.memberImageHolderImage.visibility = View.GONE
        }
        else{
            binding.memberImageView.visibility = View.INVISIBLE
            binding.memberImageHolder.visibility = View.VISIBLE
            binding.memberImageHolderImage.visibility = View.VISIBLE
        }

    }

    private fun checkForChanges(): Boolean{
        return (viewModel.member.value!!.name != binding.memberNameText.text?.trim().toString()) ||
                (viewModel.member.value!!.phone.toString() != binding.memberPhoneText.text?.trim().toString())
                || (viewModel.member.value!!.memberProfile != viewModel.updatedUri)
    }

}