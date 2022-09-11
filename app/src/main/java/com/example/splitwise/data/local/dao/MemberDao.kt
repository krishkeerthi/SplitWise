package com.example.splitwise.data.local.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitwise.data.local.entity.Member

@Dao
interface MemberDao {

//    @Query("SELECT phone FROM member WHERE name = :name")
//    suspend fun getPhoneNumberFromName(name: String): Int?
//
//    @Query("SELECT name FROM member WHERE phone = :number")
//    suspend fun getNameFromPhoneNumber(number: Int): String?

    @Query("SELECT * FROM member WHERE member_id = :memberId")
    suspend fun getMember(memberId: Int): Member?

    @Query("SELECT * FROM member")
    suspend fun getMembers(): List<Member>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: Member): Long

    @Query("SELECT * FROM member WHERE name = :name AND phone = :phoneNumber")
    suspend fun getMember(name: String, phoneNumber: Long): Member?

    @Query("UPDATE member SET member_profile = :uri WHERE member_id = :memberId")
    suspend fun updateMemberProfile(memberId: Int, uri: Uri)

    @Query("UPDATE member SET member_profile = :uri WHERE member_id = :memberId")
    suspend fun deleteMemberProfile(memberId: Int, uri: Uri)

    @Query("UPDATE member SET name = :name, phone = :phone WHERE member_id = :memberId")
    suspend fun updateMember(memberId: Int, name: String, phone: Long)

}