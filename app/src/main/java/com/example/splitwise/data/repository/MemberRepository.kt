package com.example.splitwise.data.repository

import android.content.Context
import android.view.Display
import androidx.room.util.dropFtsSyncTriggers
import com.example.splitwise.data.datasource.MemberDataSource
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.local.entity.MemberStreak
import com.example.splitwise.data.local.localdatasource.MemberLocalDataSource
import com.example.splitwise.util.titleCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MemberRepository(
    database: SplitWiseRoomDatabase
) {

    private var dataSource: MemberDataSource = MemberLocalDataSource(
        database.memberDao(),
        database.memberStreakDao()
    )

    suspend fun addMember(name: String, phoneNumber: Long): Int{
        return withContext(Dispatchers.IO){dataSource.addMember(name.titleCase(), phoneNumber)}
    }

    suspend fun getMember(memberId: Int): Member? {
        return withContext(Dispatchers.IO){dataSource.getMember(memberId)}
    }

    suspend fun getMembers(): List<Member>?{
        return withContext(Dispatchers.IO){
            dataSource.getMembers()
        }
    }

    suspend fun addMemberStreak(memberId: Int) {
        return withContext(Dispatchers.IO){
            dataSource.addMemberStreak(memberId)
        }
    }

    suspend fun getMemberStreak(memberId: Int): MemberStreak?{
        return withContext(Dispatchers.IO){
            dataSource.getMemberStreak(memberId)
        }
    }

    suspend fun getMembersStreak(memberIds: List<Int>): List<MemberStreak>?{
        return withContext(Dispatchers.IO){
            dataSource.getMembersStreak(memberIds)
        }
    }

    suspend fun incrementStreak(memberId: Int) {
        withContext(Dispatchers.IO){
            dataSource.getMemberStreak(memberId)?.let { memberStreak ->
                dataSource.updateMemberStreak(memberStreak.memberId, memberStreak.streak + 1)
            }
        }
    }

}