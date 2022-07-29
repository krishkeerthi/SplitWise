package com.example.splitwise.ui.fragment.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentSettingsBinding
import com.example.splitwise.ui.activity.main.MainActivity


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSettingsBinding.bind(view)
        sharedPreferences = requireActivity().getSharedPreferences("com.example.splitwise.sharedprefkey", Context.MODE_PRIVATE)

        // Set default radio checked

        when(sharedPreferences.getInt("THEME", R.style.Theme_SplitWise_Showcase)){
            R.style.Theme_SplitWise_Showcase ->{
                binding.themeShowcaseRadioButton.isChecked = true
            }
            R.style.Theme_SplitWise_Expression ->{
                binding.themeExpressionRadioButton.isChecked = true
            }
        }


        // Radio checked listener
        binding.themeGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.theme_showcase_radio_button -> {
                    applyTheme(R.style.Theme_SplitWise_Showcase)
                }
                R.id.theme_expression_radio_button -> {
                    applyTheme(R.style.Theme_SplitWise_Expression)
                }
                else ->{
                    applyTheme(R.style.Theme_SplitWise_Showcase)
                }
            }
        }

    }

    private fun applyTheme(themeId: Int) {
        with(sharedPreferences.edit()) {
            putInt("THEME", themeId)
            apply()
        }

        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
        requireActivity().overridePendingTransition(0, 0)
    }
}