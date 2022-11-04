//package com.example.splitwise.presentation.groups
//
//import android.app.Application
//import android.net.Uri
//import androidx.lifecycle.viewModelScope
//import com.example.splitwise.framework.Interactors
//import com.example.splitwise.framework.SplitwiseViewModel
//import com.example.splitwise.ui.SplitwiseApplication
//import kotlinx.coroutines.launch
//import java.util.*
//
//class MyGroupsViewModel(application: Application, interactors: Interactors)
//    : SplitwiseViewModel(application, interactors) {
//
//        fun createDummyGroup(name: String,
//                             description: String,
//                             date: Date,
//                             expense: Float,
//                             icon: Uri?){
//            viewModelScope.launch {
//                interactors.groupInteractors.createGroup(name, description, date, expense, icon)
//            }
//        }
//}