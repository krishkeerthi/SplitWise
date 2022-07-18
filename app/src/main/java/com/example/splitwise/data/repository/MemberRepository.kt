package com.example.splitwise.data.repository

import android.content.Context
import com.example.splitwise.data.datasource.MemberDataSource
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.local.localdatasource.MemberLocalDataSource

class MemberRepository(
    private val database: SplitWiseRoomDatabase
) {

    private var dataSource: MemberDataSource = MemberLocalDataSource(
        database.memberDao()
    )

    suspend fun addMember(name: String, phoneNumber: Long): Int{
        return dataSource.addMember(name, phoneNumber)
    }

    suspend fun getMember(memberId: Int): Member? {
        return dataSource.getMember(memberId)
    }
}