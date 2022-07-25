package com.example.splitwise.ui.activity.main

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.splitwise.R
import com.example.splitwise.databinding.ActivityMainBinding
import com.example.splitwise.ui.activity.register.RegisterActivity

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    //private val sharedPreference = getSharedPreferences(KEY, Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val sharedPreference = getSharedPreferences(KEY, Context.MODE_PRIVATE)
        val memberId = sharedPreference.getInt("MEMBERID", -1)

        Log.d(TAG, "onCreate: before register activity intent")
        if(memberId == -1){
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        Log.d(TAG, "onCreate: after register activity intent")
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
//
//        navController.setGraph(R.navigation.navigation_graph)
//
//        binding.bottomNavigation.setupWithNavController(navController)
//
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.groupsFragment,
//                R.id.splitWiseFragment,
//                R.id.groupsOverviewFragment
//            )
        //topLevelDestinationIds - The set of destinations by id considered at the top level
            // of your information hierarchy. The Up button will not be displayed when on
            // these destinations.
//        )


//        setupActionBarWithNavController(navController, appBarConfiguration)

        setSupportActionBar(binding.toolbar)
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)

        navController.addOnDestinationChangedListener(this)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
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
            if(destination.id in setOf(R.id.groupsFragment, R.id.splitWiseFragment, R.id.groupsOverviewFragment))
                View.VISIBLE
        else
            View.GONE
    }


    companion object{
        const val KEY = "com.example.splitwise.sharedprefkey"
    }
}