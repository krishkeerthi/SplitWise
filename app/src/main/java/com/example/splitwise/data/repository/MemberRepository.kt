package com.example.splitwise.data.repository

import android.content.Context
import android.view.Display
import com.example.splitwise.data.datasource.MemberDataSource
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.local.localdatasource.MemberLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemberRepository(
    private val database: SplitWiseRoomDatabase
) {

    private var dataSource: MemberDataSource = MemberLocalDataSource(
        database.memberDao()
    )

    suspend fun addMember(name: String, phoneNumber: Long): Int{
        return withContext(Dispatchers.IO){dataSource.addMember(name, phoneNumber)}
    }

    suspend fun getMember(memberId: Int): Member? {
        return withContext(Dispatchers.IO){dataSource.getMember(memberId)}
    }

    suspend fun getMembers(): List<Member>?{
        return withContext(Dispatchers.IO){
            dataSource.getMembers()
        }
    }
}