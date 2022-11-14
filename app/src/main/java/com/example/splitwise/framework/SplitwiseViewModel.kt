package com.example.splitwise.framework

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.androidcore.interactors.group.GroupInteractors
import com.example.splitwise.ui.SplitwiseApplication

open class SplitwiseViewModel(application: Application, protected val interactors: Interactors)
    :AndroidViewModel(application){

}