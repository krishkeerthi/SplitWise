package com.example.splitwise.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ConfigurationChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            // This takes care, so no need. :)
        }

    }
}