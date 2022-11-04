package com.example.androidcore.domain.group

import android.net.Uri
import java.util.*

data class GroupModel(
    val groupId: Int,
    val groupName: String,
    val description: String,
    val creationDate: Date,
    val lastActiveDate: Date,
    val totalExpense: Float,
    var groupIcon: Uri?
)

