package com.example.splitwise.data.local.dao

import androidx.room.*
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.local.entity.MemberStreak

@Dao
interface MemberStreakDao {
    @Query("SELECT * FROM member_streak WHERE member_id = :memberId")
    suspend fun getMemberStreak(memberId: Int): MemberStreak?

    @Query("SELECT * FROM member_streak WHERE member_id IN (:memberIds) ORDER BY streak DESC")
    suspend fun getMembersStreak(memberIds: List<Int>): List<MemberStreak>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memberStreak: MemberStreak): Long

    @Query("UPDATE member_streak SET streak = :streak WHERE member_id = :memberId")
    suspend fun update(memberId: Int, streak: Int): Int

}