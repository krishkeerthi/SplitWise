package com.example.splitwise.ui.activity.main

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.example.splitwise.R
import com.example.splitwise.databinding.ActivityMainBinding
import com.example.splitwise.ui.activity.register.RegisterActivity
import com.example.splitwise.ui.fragment.groups.GroupsFragment
import com.example.splitwise.ui.fragment.groupsoverview.GroupsOverviewFragment
import com.example.splitwise.ui.fragment.settings.SettingsFragment
import com.example.splitwise.ui.fragment.splitwise.SplitWiseFragment
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import java.util.*


class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    //private val sharedPreference = getSharedPreferences(KEY, Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreference = getSharedPreferences(KEY, Context.MODE_PRIVATE)
        val memberId = sharedPreference.getInt("MEMBERID", -1)

        // set shape
        val shape = sharedPreference.getInt("SHAPE", R.style.Theme_SplitWise)

        setTheme(shape)

        // set mode
        val theme = sharedPreference.getString("THEME", "Light")

        setMode(theme)

        // set language
        val language = sharedPreference.getString("LANGUAGE", "English") as String
        //Toast.makeText(this, "$language", Toast.LENGTH_SHORT).show()
        changeLanguage(language)

//        if(this.isDarkThemeOn()){
//            Toast.makeText(this, "dark theme on", Toast.LENGTH_SHORT).show()
//        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        Log.d(TAG, "onCreate: before register activity intent")
        if (memberId == -1) {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        Log.d(TAG, "onCreate: after register activity intent")
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

//
//        navController.setGraph(R.navigation.navigation_graph)
//
        binding.bottomNavigation.setupWithNavController(navController)
////
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.groupsFragment,
                R.id.splitWiseFragment,
                R.id.groupsOverviewFragment,
                R.id.settingsFragment
            )
        )
        //topLevelDestinationIds - The set of destinations by id considered at the top level
        // of your information hierarchy. The Up button will not be displayed when on
        // these destinations.
//        )


        setupActionBarWithNavController(navController, appBarConfiguration)
        // this give up navigation and fragment names for title, explicit change not applying

        //   setSupportActionBar(binding.toolbar)
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)

        navController.addOnDestinationChangedListener(this)

        val colorDrawable = ColorDrawable(Color.parseColor("#0F9D58"))
        actionBar?.setBackgroundDrawable(colorDrawable)

        // testing, not working
//        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
//            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
//                TransitionManager.beginDelayedTransition(binding.root, Slide(Gravity.BOTTOM).excludeTarget(R.id.nav_host_fragment_container, true))
//                when (f) {
//                    is GroupsFragment -> {
//                        binding.bottomNavigation.visibility = View.VISIBLE
//                    }
//                    is SplitWiseFragment -> {
//                        binding.bottomNavigation.visibility = View.VISIBLE
//                    }
//                    is GroupsOverviewFragment -> {
//                        binding.bottomNavigation.visibility = View.VISIBLE
//                    }
//                    is SettingsFragment -> {
//                        binding.bottomNavigation.visibility = View.VISIBLE
//                    }
//                    else -> {
//                        binding.bottomNavigation.visibility = View.VISIBLE
//                    }
//                }
//            }
//        }, true)
    }

    private fun setMode(theme: String?) {
        when(theme) {
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }


    private fun changeLanguage(language: String) {
        val languageCode = when(language){
            "English" -> "en"
            "Tamil" -> "ta"
            else -> "en"
        }
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        Log.d(TAG, "changeLanguage: $languageCode")
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }


        override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {

        binding.bottomNavigation.visibility =
            if (destination.id in setOf(
                    R.id.groupsFragment, R.id.splitWiseFragment, R.id.groupsOverviewFragment,
                    R.id.settingsFragment
                )
            )
                View.VISIBLE
            else {
                View.GONE
            }// View.INVISIBLE not good
    }

//  override fun onBackPressed() {

        //val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container)

//        val currentFragment = navController.currentDestination?.id
//
//        if(currentFragment == R.id.searchGroupFragment){
//             val fragment = supportFragmentManager.findFragmentById(R.id.searchGroupFragment)
////
//            Log.d(TAG, "onBackPressed: search group fragment ${fragment == null}")
//            if(fragment != null)
//            (fragment as SearchGroupFragment).onBackPressed()
//            else
//                super.onBackPressed()
//        }
//        else{
//            super.onBackPressed()
//        }

//        val fragmentList: List<Fragment> = supportFragmentManager.fragments
//        Log.d(TAG, "onBackPressed: ${fragmentList}")
//        for (f in fragmentList) {
//            if (f is SearchGroupFragment) {
//                f.onBackPressed()
//            }
//            else
//                super.onBackPressed()
//        }

 //   }

    companion object {
        const val KEY = "com.example.splitwise.sharedprefkey"
    }
}