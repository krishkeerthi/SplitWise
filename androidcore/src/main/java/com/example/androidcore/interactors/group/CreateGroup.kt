package com.example.androidcore.interactors.group

import android.net.Uri
import com.example.androidcore.data.group.MyGroupRepository
import java.util.*

class CreateGroup(private val myGroupRepository: MyGroupRepository) {
    suspend operator fun invoke(name: String, description: String, date: Date, expense: Float, icon: Uri?): Int{
        return myGroupRepository.createGroup(name, description, date, expense, icon)
    }
}