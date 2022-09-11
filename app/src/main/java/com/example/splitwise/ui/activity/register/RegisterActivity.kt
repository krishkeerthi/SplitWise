package com.example.splitwise.ui.activity.register

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.splitwise.R
import com.example.splitwise.databinding.ActivityRegisterBinding
import com.example.splitwise.ui.activity.main.MainActivity
import com.example.splitwise.util.nameCheck

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterActivityViewModel by viewModels {
        RegisterActivityViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val sharedPreference = getSharedPreferences(MainActivity.KEY, Context.MODE_PRIVATE)

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
                } else {
                    nameLayout.error = getString(R.string.enter_valid_name)
                }

                binding.registerButton.isEnabled =
                    (nameCheck(nameEditText.text?.trim().toString())) &&
                            (phoneEditText.text?.trim().toString().length == 10)

            }

        }

        val phoneWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (phoneEditText.text?.trim().toString().length == 10) {
                    phoneLayout.error = null
                    phoneLayout.isErrorEnabled = false

                } else {
                    phoneLayout.error = getString(R.string.enter_10_numbers)

                }

                binding.registerButton.isEnabled =
                    (nameCheck(nameEditText.text?.trim().toString())) &&
                            (phoneEditText.text?.trim().toString().length == 10)
            }

        }

        nameEditText.addTextChangedListener(nameWatcher)
        phoneEditText.addTextChangedListener(phoneWatcher)

        // Button click
        binding.registerButton.setOnClickListener {
            viewModel.registerMember(
                binding.memberNameText.text.toString(),
                binding.memberPhoneText.text.toString().toLong()
            )
        }

        // Live data(observe to move to next screen)

        viewModel.memberId.observe(this) {
            if (it != null) {
                saveIdToSharedPreference(it, sharedPreference)
                gotoMainActivity()
            }
        }
    }

    override fun onBackPressed() {
        finishAffinity()
        super.onBackPressed()
    }

    private fun gotoMainActivity() {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun saveIdToSharedPreference(memberId: Int, sharedPreference: SharedPreferences) {
        val sharedPreferenceEditor = sharedPreference.edit()
        sharedPreferenceEditor.apply {
            putInt("MEMBERID", memberId)
            apply()
        }
    }
}