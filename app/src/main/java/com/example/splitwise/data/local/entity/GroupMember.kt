package com.example.splitwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_member")
data class GroupMember(
    @ColumnInfo(name = "group_id")
    val groupId: Int,
    @ColumnInfo(name = "member_id")
    val memberId: Int
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "group_member_id")
    var groupMemberId: Int= 0
}
