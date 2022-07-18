package com.example.splitwise.data.local.localdatasource

import com.example.splitwise.data.datasource.MemberDataSource
import com.example.splitwise.data.local.dao.MemberDao
import com.example.splitwise.data.local.entity.Member

class MemberLocalDataSource(
    private val memberDao: MemberDao
): MemberDataSource {
    override suspend fun addMember(name: String, phoneNumber: Long): Int {
        return memberDao.insert(
            Member(
                name, phoneNumber
            )).toInt()
    }

    override suspend fun getMember(memberId: Int): Member? {
        return memberDao.getMember(memberId)
    }
}