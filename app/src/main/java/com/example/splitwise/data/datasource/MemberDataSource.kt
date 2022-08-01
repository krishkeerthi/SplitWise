package com.example.splitwise.data.datasource

import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.local.entity.MemberStreak

interface MemberDataSource {

    suspend fun addMember(name: String, phoneNumber: Long): Int

    suspend fun getMember(memberId: Int): Member?

    suspend fun getMembers(): List<Member>?

    suspend fun addMemberStreak(memberId: Int): Int

    suspend fun getMemberStreak(memberId: Int): MemberStreak?

    suspend fun getMembersStreak(memberIds: List<Int>): List<MemberStreak>?

    suspend fun updateMemberStreak(memberId: Int, streak: Int)
}