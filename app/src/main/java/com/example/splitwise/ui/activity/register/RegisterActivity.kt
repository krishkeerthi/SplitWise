package com.example.splitwise.ui.activity.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
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
                binding.registerButton.isEnabled = if (nameCheck(nameEditText.text?.trim().toString())) {
                    nameLayout.error = null
                    nameLayout.isErrorEnabled = false
                    true
                }
                else {
                    nameLayout.error = "Enter valid name"
                    false
                }

            }

        }

        val phoneWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.registerButton.isEnabled = if (phoneEditText.text?.trim().toString().length == 10) {
                    phoneLayout.error = null
                    phoneLayout.isErrorEnabled = false
                    true
                }
                else {
                    phoneLayout.error = "Enter valid name"
                    false
                }

            }

        }

        nameEditText.addTextChangedListener(nameWatcher)
        phoneEditText.addTextChangedListener(phoneWatcher)

        // Button click
        binding.registerButton.setOnClickListener{
            viewModel.registerMember(binding.memberNameText.text.toString(), binding.memberPhoneText.text.toString().toLong())
        }

        // Live data(observe to move to next screen)

        viewModel.registered.observe(this){
            if(it){
                gotoMainActivity()
            }
        }
    }

    private fun gotoMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}