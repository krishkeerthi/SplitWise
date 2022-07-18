package com.example.splitwise.data.local.localdatasource

import com.example.splitwise.data.datasource.GroupDataSource
import com.example.splitwise.data.local.dao.GroupDao
import com.example.splitwise.data.local.dao.GroupExpenseDao
import com.example.splitwise.data.local.dao.GroupMemberDao
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.GroupExpense
import com.example.splitwise.data.local.entity.GroupMember
import java.util.*

class GroupLocalDataSource(
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val groupExpenseDao: GroupExpenseDao
):
GroupDataSource{
    override suspend fun createGroup(
        name: String,
        description: String,
        date: Date,
        expense: Float
    ): Int {
        return groupDao.insert(
            Group(
                name,
                description,
                date,
                date,
                expense
            )).toInt()
    }

    override suspend fun addGroupMember(groupId: Int, memberId: Int) {
        groupMemberDao.insert(GroupMember(groupId, memberId))
    }

    override suspend fun addGroupExpense(groupId: Int, expenseId: Int) {
        groupExpenseDao.insert(GroupExpense(groupId, expenseId))
    }

    override suspend fun getGroup(groupId: Int): Group?{
        return groupDao.getGroup(groupId)
    }

    override suspend fun getGroups(): List<Group>? {
        return groupDao.getGroups()
    }

    override suspend fun getGroupMembers(groupId: Int): List<Int>?{
        return groupMemberDao.getGroupMembers(groupId)
    }

    // groups with constraint pending
}