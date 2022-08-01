package com.example.splitwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "member_streak")
data class MemberStreak(
    @ColumnInfo(name = "member_id")
    val memberId: Int,
    val streak: Int
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "member_streak_id")
    var memberStreakId: Int = 0
}
