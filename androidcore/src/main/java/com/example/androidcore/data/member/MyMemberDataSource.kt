package com.example.androidcore.data.member

import android.net.Uri
import com.example.androidcore.domain.member.MemberModel
import com.example.androidcore.domain.member.MemberStreakModel

interface MyMemberDataSource {

    suspend fun addMember(name: String, phoneNumber: Long, uri: Uri?): Int

    suspend fun getMember(memberId: Int): MemberModel?

    suspend fun getMembers(): List<MemberModel>?

    suspend fun addMemberStreak(memberId: Int, defaultStreak: Int): Int

    suspend fun getMemberStreak(memberId: Int): MemberStreakModel?

    suspend fun getMembersStreak(memberIds: List<Int>): List<MemberStreakModel>?

    suspend fun updateMemberStreak(memberId: Int, streak: Int)

    suspend fun getMember(name: String, phoneNumber: Long): MemberModel?

    suspend fun updateMember(memberId: Int, name: String, phone: Long)

    suspend fun updateProfile(memberId: Int, uri: Uri?)
}