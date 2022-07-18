package com.example.splitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitwise.data.local.entity.GroupMember

@Dao
interface GroupMemberDao {

    @Query("SELECT member_id FROM group_member WHERE group_id = :groupId")
    suspend fun getGroupMembers(groupId: Int): List<Int>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(groupMember: GroupMember)
}