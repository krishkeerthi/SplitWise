package com.example.splitwise.ui.fragment.addamount

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.DialogFragment
import com.example.splitwise.R
import com.example.splitwise.ui.fragment.groups.GroupsViewModel
import com.example.splitwise.util.AmountFilter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddAmountDialog(private val viewModel: GroupsViewModel, val createChip: (AmountFilter, Float, Context) -> Unit): DialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.enter_amount)

        val amountDialog = layoutInflater.inflate(R.layout.add_amount_dialog, null)
        builder.setView(amountDialog)

        val amountEditText = amountDialog.findViewById<TextInputEditText>(R.id.amount_text)
        val amountLayout =
            amountDialog.findViewById<TextInputLayout>(R.id.outlined_amount_text_field)

        builder.setPositiveButton(getString(R.string.save)) { dialogInterface, _ ->
            val amount = amountEditText.text.toString().toFloat()


            viewModel.applyAmountFilter(amount)
            createChip(viewModel.selectedAmountFilter, amount, requireContext())
            //createAmountFilterChip(viewModel.selectedAmountFilter, amount)
        }

        builder.setNegativeButton(getString(R.string.cancel)){ dialogInterface, _ ->
            dialogInterface.cancel()
        }

        val dialog = builder.create()
        dialog.show()


        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        val amountWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val amountText = amountEditText.text.toString()

                val amount = if(amountText.isNotEmpty()) amountText.toInt() else 0



                if (amount == 0 || amountText.isEmpty())
                    amountLayout.error = getString(R.string.enter_amount)
                else {
                    amountLayout.error = null
                    amountLayout.isErrorEnabled = false
                }

                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = amountText.isNotEmpty()
            }
        }
        amountEditText.addTextChangedListener(amountWatcher)

        return dialog
    }
}