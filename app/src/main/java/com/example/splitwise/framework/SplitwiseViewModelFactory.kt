package com.example.splitwise.framework

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidcore.interactors.group.GroupInteractors
import com.example.splitwise.ui.SplitwiseApplication

object SplitwiseViewModelFactory: ViewModelProvider.Factory{
    private lateinit var application: SplitwiseApplication
    private lateinit var interactors: Interactors

    fun inject(application: SplitwiseApplication, interactors: Interactors){
        SplitwiseViewModelFactory.application = application
        SplitwiseViewModelFactory.interactors = interactors
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(SplitwiseViewModel::class.java.isAssignableFrom(modelClass)){
            return modelClass.getConstructor(Application::class.java, Interactors::class.java)
                .newInstance(
                    application,
                    interactors
                )
        }
        else
        {
            throw java.lang.IllegalStateException("Viewmodel should extend splitwise viewmodel")

        }
    }

}