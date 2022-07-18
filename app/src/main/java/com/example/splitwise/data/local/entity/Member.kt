package com.example.splitwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Member(
    val name: String,
    val phone: Long,
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "member_id")
    var memberId: Int = 0
}
