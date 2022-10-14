package com.example.splitwise.data.repository

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
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

    suspend fun addMember(name: String, phoneNumber: Long, uri: Uri?): Int{
        return withContext(Dispatchers.IO){dataSource.addMember(name.titleCase(), phoneNumber, uri)}
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

    suspend fun decrementStreak(memberId: Int) {
        withContext(Dispatchers.IO){
            dataSource.getMemberStreak(memberId)?.let { memberStreak ->
                if(memberStreak.streak > 0)
                    dataSource.updateMemberStreak(memberStreak.memberId, memberStreak.streak - 1)
            }
        }
    }

    suspend fun checkMemberExistence(name: String, phoneNumber: Long): Boolean {
        return withContext(Dispatchers.IO){
            val result = dataSource.getMember(name.titleCase(), phoneNumber)
            Log.d(TAG, "checkMemberExistence: ${result != null}")
            result != null
        }
    }

    suspend fun updateMember(memberId: Int, name: String, phone: Long) {
        Log.d(ContentValues.TAG, "onViewCreated: repo update member${name}  ${phone}")
        withContext(Dispatchers.IO){
            dataSource.updateMember(memberId, name, phone)
        }

    }

    suspend fun updateMemberProfile(memberId: Int, uri: Uri?) {
        withContext(Dispatchers.IO){
            dataSource.updateProfile(memberId, uri)
        }
    }


}