package com.example.splitwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Group(
    @ColumnInfo(name = "group_name")
    val groupName: String,
    val description: String,
    @ColumnInfo(name = "creation_date")
    val creationDate: Date,
    @ColumnInfo(name = "last_active_date")
    val lastActiveDate: Date,
    @ColumnInfo(name = "total_expense")
    val totalExpense: Float
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "group_id")
    var groupId: Int = 0

    override fun toString(): String {
        return groupName
    }
}
