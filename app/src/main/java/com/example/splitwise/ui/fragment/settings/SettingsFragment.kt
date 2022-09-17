package com.example.splitwise.ui.fragment.settings

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentSettingsBinding
import com.example.splitwise.ui.activity.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import java.util.*


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(requireContext())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.settings)
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSettingsBinding.bind(view)

        sharedPreferences = requireActivity().getSharedPreferences(
            "com.example.splitwise.sharedprefkey",
            Context.MODE_PRIVATE
        )

        // Set default radio checked

        when (sharedPreferences.getInt("SHAPE", R.style.Theme_SplitWise)) {
            R.style.Theme_SplitWise -> {
                binding.shapeDefaultRadioButton.isChecked = true
            }
            R.style.Theme_SplitWise_Boxed -> {
                binding.shapeBoxedRadioButton.isChecked = true
            }
        }


        when (sharedPreferences.getString("LANGUAGE", "English")) {
            "English" -> {
                binding.languageDefaultRadioButton.isChecked = true
            }
            "Tamil" -> {
                binding.languageTamilRadioButton.isChecked = true
            }
        }

        when (sharedPreferences.getString("THEME", "Light")) {
            "Light" -> {
                binding.themeLightButton.isChecked = true
            }
            "Dark" -> {
                binding.themeDarkButton.isChecked = true
            }
        }


        // Radio checked listener
        binding.shapeGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.shape_default_radio_button -> {
                    applyTheme(R.style.Theme_SplitWise)
                }
                R.id.shape_boxed_radio_button -> {
                    applyTheme(R.style.Theme_SplitWise_Boxed)
                }
                else -> {
                    applyTheme(R.style.Theme_SplitWise)
                }
            }
        }

        binding.languageGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.language_default_radio_button -> {
                    changeLanguage("English")
                }
                R.id.language_tamil_radio_button -> {
                    changeLanguage("Tamil")
                }
                else -> {
                    changeLanguage("English")
                }
            }
        }

        binding.themeGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.theme_light_button -> {
                    changeMode("Light")
                }
                R.id.theme_dark_button -> {
                    changeMode("Dark")
                }
                else -> {
                    changeMode("Light")
                }
            }
        }

        binding.feedback.setOnClickListener{
            gotoFeedbackFragment()
        }


        if (!sharedPreferences.getBoolean("DATA_INSERTED", false)) {
            setHasOptionsMenu(true)
        }

    }

    private fun gotoFeedbackFragment() {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

//        val transitionName = getString(R.string.feedback_transition_name)
//        val extras = FragmentNavigatorExtras(binding.feedback to transitionName)
        val action = SettingsFragmentDirections.actionSettingsFragmentToFeedbackFragment()
        findNavController().navigate(action)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.settings_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.import_menu -> {
                viewModel.insertSampleData()
                updateDataInserted()
                Snackbar.make(binding.root, getString(R.string.data_imported), Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.import_menu).isVisible =
            !sharedPreferences.getBoolean("DATA_INSERTED", false)

        super.onPrepareOptionsMenu(menu)
    }

    private fun updateDataInserted() {
        with(sharedPreferences.edit()) {
            putBoolean("DATA_INSERTED", true)
            apply()
        }
        requireActivity().invalidateOptionsMenu()
    }

    private fun restartActivity() {
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
        requireActivity().overridePendingTransition(0, 0)
    }

    private fun applyTheme(themeId: Int) {
        with(sharedPreferences.edit()) {
            putInt("SHAPE", themeId)
            apply()
        }

        setTheme(themeId)
    }

    private fun changeLanguage(language: String) {
        with(sharedPreferences.edit()) {
            putString("LANGUAGE", language)
            apply()
        }

        setLanguage(language)
    }

    private fun changeMode(theme: String) {
        with(sharedPreferences.edit()) {
            putString("THEME", theme)
            apply()
        }

        setMode(theme)
    }

    private fun setMode(theme: String?) {
        when(theme) {
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setLanguage(language: String) {
        val languageCode = when(language){
            "English" -> "en"
            "Tamil" -> "ta"
            else -> "en"
        }
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        Log.d(ContentValues.TAG, "changeLanguage: $languageCode")
        val config = resources.configuration
        config.setLocale(locale)
        requireActivity().createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)

        requireActivity().recreate()
    }

    private fun setTheme(resId: Int) {
        (requireActivity() as AppCompatActivity).delegate.setTheme(resId)

        requireActivity().recreate()
    }

}