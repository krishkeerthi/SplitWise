package com.example.splitwise.ui

import android.app.Application
//import com.example.androidcore.data.group.MyGroupRepository
//import com.example.androidcore.interactors.group.GroupInteractors
//import com.example.splitwise.data.local.localdatasource.GroupLocalDataSource
//import com.example.splitwise.framework.Interactors
//import com.example.splitwise.framework.SplitwiseViewModelFactory
//import com.example.splitwise.framework.db.group.MyGroupLocalDataSource

class SplitwiseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        application = this

//        val myGroupRepository = MyGroupRepository(MyGroupLocalDataSource(this))
//
//        SplitwiseViewModelFactory.inject(
//            this,
//            Interactors(
//                GroupInteractors(myGroupRepository)
//            )
//        )
    }

    companion object {
        private lateinit var application: SplitwiseApplication

        fun getApplication() = application
    }
}