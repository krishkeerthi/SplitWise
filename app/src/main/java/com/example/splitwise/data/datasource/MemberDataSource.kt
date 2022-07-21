package com.example.splitwise.data.datasource

import com.example.splitwise.data.local.entity.Member

interface MemberDataSource {

    suspend fun addMember(name: String, phoneNumber: Long): Int

    suspend fun getMember(memberId: Int): Member?

    suspend fun getMembers(): List<Member>?
}