package com.example.splitwise.framework.db.member

import android.content.Context
import android.net.Uri
import com.example.androidcore.data.member.MyMemberDataSource
import com.example.androidcore.domain.member.MemberModel
import com.example.androidcore.domain.member.MemberStreakModel
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.dao.MemberDao
import com.example.splitwise.data.local.dao.MemberStreakDao

class MyMemberLocalDataSource(context: Context): MyMemberDataSource {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberDao: MemberDao = database.memberDao()
    private val memberStreakDao: MemberStreakDao = database.memberStreakDao()

    override suspend fun addMember(name: String, phoneNumber: Long, uri: Uri?): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getMember(memberId: Int): MemberModel? {
        var memberModel: MemberModel? = null

        memberDao.getMember(memberId)?.let {
            memberModel = MemberModel(it.memberId, it.name, it.phone, it.memberProfile)
        }

        return memberModel
    }

    override suspend fun getMember(name: String, phoneNumber: Long): MemberModel? {
        TODO("Not yet implemented")
    }

    override suspend fun getMembers(): List<MemberModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun addMemberStreak(memberId: Int, defaultStreak: Int): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberStreak(memberId: Int): MemberStreakModel? {
        TODO("Not yet implemented")
    }

    override suspend fun getMembersStreak(memberIds: List<Int>): List<MemberStreakModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun updateMemberStreak(memberId: Int, streak: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun updateMember(memberId: Int, name: String, phone: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateProfile(memberId: Int, uri: Uri?) {
        TODO("Not yet implemented")
    }

}