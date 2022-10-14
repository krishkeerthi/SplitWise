package com.example.splitwise.data.local.localdatasource

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.room.PrimaryKey
import com.example.splitwise.data.datasource.MemberDataSource
import com.example.splitwise.data.local.dao.MemberDao
import com.example.splitwise.data.local.dao.MemberStreakDao
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.local.entity.MemberStreak

class MemberLocalDataSource(
    private val memberDao: MemberDao,
    private val memberStreakDao: MemberStreakDao
): MemberDataSource {
    override suspend fun addMember(name: String, phoneNumber: Long, uri: Uri?): Int {
        return memberDao.insert(
            Member(
                name, phoneNumber, uri
            )).toInt()
    }

    override suspend fun getMember(memberId: Int): Member? {
        return memberDao.getMember(memberId)
    }

    override suspend fun getMembers(): List<Member>? {
        return memberDao.getMembers()
    }

    override suspend fun addMemberStreak(memberId: Int): Int {
        return memberStreakDao.insert(MemberStreak(memberId, 0)).toInt()
    }

    override suspend fun getMemberStreak(memberId: Int): MemberStreak? {
        return memberStreakDao.getMemberStreak(memberId)
    }

    override suspend fun getMembersStreak(memberIds: List<Int>): List<MemberStreak>? {
        return memberStreakDao.getMembersStreak(memberIds)
    }

    override suspend fun updateMemberStreak(memberId: Int, streak: Int) {
        memberStreakDao.update(memberId, streak)
    }

    override suspend fun updateMember(memberId: Int, name: String, phone: Long) {
        Log.d(ContentValues.TAG, "onViewCreated: lds update member${name}  ${phone}")
        memberDao.updateMember(memberId, name, phone)

        val x = memberDao.getMember(memberId)
        Log.d(ContentValues.TAG, "onViewCreated: vm update member${x?.name}  ${x?.phone}")
    }

    override suspend fun updateProfile(memberId: Int, uri: Uri?) {
        memberDao.updateMemberProfile(memberId, uri)
    }

    override suspend fun getMember(name: String, phoneNumber: Long): Member? {
        return memberDao.getMember(name, phoneNumber)
    }

}