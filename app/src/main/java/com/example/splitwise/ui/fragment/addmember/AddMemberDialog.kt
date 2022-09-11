package com.example.splitwise.ui.fragment.addmember

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.example.splitwise.R
import com.example.splitwise.ui.fragment.createeditgroup.CreateEditGroupViewModel
import com.example.splitwise.util.formatNumber
import com.example.splitwise.util.nameCheck
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddMemberDialog(
    private val viewModel: CreateEditGroupViewModel
) : DialogFragment() {

    //private val viewModel: CreateEditGroupViewModel by activityViewModels()

    private val contactNameLiveData = MutableLiveData<String>()
    private val contactPhoneLiveData = MutableLiveData<String>()

    // note: In dialog on create view callback is not called

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true

        val builder = AlertDialog.Builder(requireContext())

        val addMemberDialog = layoutInflater.inflate(R.layout.add_member_dialog, null)

        val nameEditText = addMemberDialog.findViewById<TextInputEditText>(R.id.member_name_text)
        val phoneEditText = addMemberDialog.findViewById<TextInputEditText>(R.id.member_phone_text)

        val selectContactButton =
            addMemberDialog.findViewById<MaterialButton>(R.id.select_contact_button)

        val nameLayout =
            addMemberDialog.findViewById<TextInputLayout>(R.id.outlined_member_name_text_field)
        val phoneLayout =
            addMemberDialog.findViewById<TextInputLayout>(R.id.outlined_member_phone_text_field)


        selectContactButton.setOnClickListener {
            // Toast.makeText(requireContext(), "Working on it", Toast.LENGTH_SHORT).show()
            selectContact()

//            if(contactName != "" && contactPhone != ""){
//                phoneEditText.setText(contactPhone)
//                nameEditText.setText(contactName)
//            }
//            else{
//                Toast.makeText(requireContext(), "Error selecting contact", Toast.LENGTH_LONG).show()
//            }
        }

        contactNameLiveData.observe(this) {
            if (it != "")
                nameEditText.setText(it)
            else
                Log.d(TAG, "onCreateDialog: name empty")
        }

        contactPhoneLiveData.observe(this) {
            if (it != "")
                phoneEditText.setText(it)
            else
                Log.d(TAG, "onCreateDialog: phone empty")
        }

        builder.apply {
            setView(addMemberDialog)
            setTitle(R.string.add_member)

            // Save button
            setPositiveButton(R.string.save) { dialogInterface, position ->
                val name = nameEditText.text.toString()
                val phoneNumber = phoneEditText.text.toString().toLong()

                viewModel.checkMember(name, phoneNumber, null) // true means member does not registered

                Log.d(TAG, "onCreateDialog: membercheck dialog saved, ${viewModel.members.value}")
            }

            setNegativeButton(R.string.cancel) { dialogInterface, position ->
                dialogInterface.cancel()
            }
        }

        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        // Text watchers
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

                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled =
                    nameCheck(nameEditText.text?.trim().toString()) &&
                            (phoneEditText.text?.trim().toString().length == 10)

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
                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled =
                    nameCheck(nameEditText.text?.trim().toString()) &&
                            (phoneEditText.text?.trim().toString().length == 10)

            }
        }

        nameEditText.addTextChangedListener(nameWatcher)
        phoneEditText.addTextChangedListener(phoneWatcher)

        return dialog
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
            }
            else
                Log.d(TAG, "result not ok: empty")

        }


}