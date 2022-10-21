package com.example.splitwise.ui

import android.app.Application

class SplitwiseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        application = this
    }

    companion object {
        private lateinit var application: SplitwiseApplication

        fun getApplication() = application
    }
}