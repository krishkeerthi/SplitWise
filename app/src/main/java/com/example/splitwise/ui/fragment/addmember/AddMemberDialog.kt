package com.example.splitwise.ui.fragment.addmember

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.splitwise.R
import com.example.splitwise.ui.fragment.createeditgroup.CreateEditGroupViewModel
import com.example.splitwise.util.nameCheck
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddMemberDialog : DialogFragment() {

    private val viewModel: CreateEditGroupViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val addMemberDialog = layoutInflater.inflate(R.layout.add_member_dialog, null)

        val nameEditText = addMemberDialog.findViewById<TextInputEditText>(R.id.member_name_text)
        val phoneEditText = addMemberDialog.findViewById<TextInputEditText>(R.id.member_phone_text)

        val nameLayout =
            addMemberDialog.findViewById<TextInputLayout>(R.id.outlined_member_name_text_field)
        val phoneLayout =
            addMemberDialog.findViewById<TextInputLayout>(R.id.outlined_member_phone_text_field)

        builder.apply {
            setView(addMemberDialog)
            setTitle("Add Member")

            setPositiveButton("Save") { dialogInterface, position ->
                viewModel.addMember(
                    nameEditText.text.toString(),
                    phoneEditText.text.toString().toLong()
                )
            }
        }

        val dialog = builder.create()

        val nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (nameCheck(nameEditText.text?.trim().toString())) {
                    nameLayout.error = null
                    nameLayout.isErrorEnabled = false
                }
                else
                    nameLayout.error = "Enter valid name"

                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled =
                    nameCheck(nameEditText.text?.trim().toString()) ||
                            (phoneEditText.text?.trim().toString().length == 10)

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
                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled =
                    nameCheck(nameEditText.text?.trim().toString()) ||
                            (phoneEditText.text?.trim().toString().length == 10)

            }

        }

        nameEditText.addTextChangedListener(nameWatcher)
        phoneEditText.addTextChangedListener(phoneWatcher)

        return dialog
    }


}