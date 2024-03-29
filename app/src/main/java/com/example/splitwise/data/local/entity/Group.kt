package com.example.splitwise.data.local.entity

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
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
    val totalExpense: Float,
    @ColumnInfo(name = "group_icon")
    var groupIcon: Uri? //  null means icon not set
): Parcelable{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "group_id")
    var groupId: Int = 0

    override fun toString(): String {
        return groupName
    }
}
