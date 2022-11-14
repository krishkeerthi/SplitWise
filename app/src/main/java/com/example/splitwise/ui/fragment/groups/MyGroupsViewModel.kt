package com.example.splitwise.ui.fragment.groups

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.splitwise.framework.Interactors
import com.example.splitwise.framework.SplitwiseViewModel
import kotlinx.coroutines.launch
import java.util.*

class MyGroupsViewModel(application: Application, interactors: Interactors) :
    SplitwiseViewModel(application, interactors) {

    private val _testLiveData = MutableLiveData<Boolean>()
    val testLiveData: LiveData<Boolean>
        get() = _testLiveData

    fun createDummyGroup(
        groupName: String,
        description: String,
        creationDate: Date,
        totalExpense: Float,
        groupIcon: Uri?
    ) {
        viewModelScope.launch {
            interactors.groupInteractors.createGroup(
                groupName,
                description,
                creationDate,
                totalExpense,
                groupIcon
            )
        }
    }

    fun testMethod(value: Boolean){
        _testLiveData.value  = value
    }
}