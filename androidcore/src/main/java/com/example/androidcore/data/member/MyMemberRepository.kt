package com.example.androidcore.data.member

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import com.example.androidcore.domain.member.MemberModel
import com.example.androidcore.domain.member.MemberStreakModel
import com.example.androidcore.util.titleCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyMemberRepository(private val dataSource: MyMemberDataSource) {

    suspend fun addMember(name: String, phoneNumber: Long, uri: Uri?): Int{
        return withContext(Dispatchers.IO){dataSource.addMember(name.titleCase(), phoneNumber, uri)}
    }

    suspend fun getMember(memberId: Int): MemberModel? {
        return withContext(Dispatchers.IO){dataSource.getMember(memberId)}
    }

    suspend fun getMembers(): List<MemberModel>?{
        return withContext(Dispatchers.IO){
            dataSource.getMembers()
        }
    }

    suspend fun addMemberStreak(memberId: Int) {
        return withContext(Dispatchers.IO){
            dataSource.addMemberStreak(memberId, 0)
        }
    }

    suspend fun getMemberStreak(memberId: Int): MemberStreakModel?{
        return withContext(Dispatchers.IO){
            dataSource.getMemberStreak(memberId)
        }
    }

    suspend fun getMembersStreak(memberIds: List<Int>): List<MemberStreakModel>?{
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
            Log.d(ContentValues.TAG, "checkMemberExistence: ${result != null}")
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

    suspend fun createOrIncrementStreak(memberId: Int) {
        withContext(Dispatchers.IO){
            val memberStreak = dataSource.getMemberStreak(memberId)
            if(memberStreak != null){
                dataSource.updateMemberStreak(memberStreak.memberId, memberStreak.streak + 1)
            }
            else{
                val memberStreakId = dataSource.addMemberStreak(memberId,1)
//                dataSource.getMemberStreak(memberStreakId)?.let {
//                    dataSource.updateMemberStreak(it.memberId,1)
//                }

            }
        }
    }
}