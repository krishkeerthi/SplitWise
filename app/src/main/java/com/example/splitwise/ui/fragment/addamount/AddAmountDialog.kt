package com.example.splitwise.ui.fragment.addamount

import android.app.ActionBar.LayoutParams
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.renderscript.ScriptGroup.Input
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import com.example.splitwise.R
import com.example.splitwise.ui.fragment.groups.GroupsViewModel
import com.example.splitwise.util.AmountFilter
import com.example.splitwise.util.showKeyboard
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddAmountDialog(private val viewModel: GroupsViewModel, val createChip: (AmountFilter, Float, Context) -> Unit): DialogFragment(){

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // It took so long time to find this constant
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.enter_amount)

        val amountDialog = layoutInflater.inflate(R.layout.add_amount_dialog, null)
        builder.setView(amountDialog)

        val amountEditText = amountDialog.findViewById<TextInputEditText>(R.id.amount_text)
        val amountLayout =
            amountDialog.findViewById<TextInputLayout>(R.id.outlined_amount_text_field)

        amountEditText.isFocusable = true
        // Request focus by default
        amountEditText.requestFocus()
//        amountEditText.isFocusable = true
//        amountEditText.isPressed = true

//        amountEditText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0f, 0f, 0))
//        amountEditText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0f, 0f, 0))

        Log.d(TAG, "onCreateDialog: soft input focused: ${amountEditText.showSoftInputOnFocus}")
       // amountEditText.performClick()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(amountLayout, InputMethodManager.SHOW_IMPLICIT)
        //imm.toggleSoftInputFromWindow(requireActivity().currentFocus?.windowToken, InputMethodManager.SHOW_FORCED, 0)
        //amountEditText.showKeyboard()

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

//        Log.d(TAG, "onCreateDialog: window available ${dialog.window != null}")
//        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        val amountWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val amountText = amountEditText.text.toString()

                val amount = if(amountText.isNotEmpty()) amountText.toInt() else 0



                if (amountText.isEmpty())
                    amountLayout.error = getString(R.string.enter_amount)
                else if(amount == 0)
                    amountLayout.error = getString(R.string.amount_greater_than_0)
                else {
                    amountLayout.error = null
                    amountLayout.isErrorEnabled = false
                }

                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = amountText.isNotEmpty() && amount > 0
            }
        }
        amountEditText.addTextChangedListener(amountWatcher)

        return dialog
    }
}