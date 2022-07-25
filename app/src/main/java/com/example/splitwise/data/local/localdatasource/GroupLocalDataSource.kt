package com.example.splitwise.data.local.localdatasource

import com.example.splitwise.data.datasource.GroupDataSource
import com.example.splitwise.data.local.dao.GroupDao
import com.example.splitwise.data.local.dao.GroupExpenseDao
import com.example.splitwise.data.local.dao.GroupMemberDao
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.GroupExpense
import com.example.splitwise.data.local.entity.GroupMember
import java.util.*
import kotlin.math.exp

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

    override suspend fun getGroupsStartsWith(query: String): List<Group>? {
        return groupDao.getGroupsStartsWith(query)
    }

    override suspend fun updateTotalExpense(groupId: Int, amount: Float) {
        groupDao.updateTotalExpense(groupId, amount)
    }

    override suspend fun getGroupMembers(groupId: Int): List<Int>?{
        return groupMemberDao.getGroupMembers(groupId)
    }

    override suspend fun getTotalExpense(groupId: Int): Float? {
        return groupDao.getTotalExpense(groupId)
    }

    override suspend fun getGroupsCreatedBefore(date: Date): List<Group>? {
        return groupDao.getGroupsCreatedBefore(date)
    }

    override suspend fun getGroupsCreatedAfter(date: Date): List<Group>? {
        return groupDao.getGroupsCreatedAfter(date)
    }

    override suspend fun getGroupsWithAmountBelow(amount: Float): List<Group>? {
        return groupDao.getGroupsWithAmountBelow(amount)
    }

    override suspend fun getGroupsWithAmountAbove(amount: Float): List<Group> {
        return groupDao.getGroupsWithAmountAbove(amount)
    }

    override suspend fun getGroupsCreatedBeforeAndAmountBelow(
        date: Date,
        amount: Float
    ): List<Group>? {
        return groupDao.getGroupsCreatedBeforeAndAmountBelow(date, amount)
    }

    override suspend fun getGroupsCreatedBeforeAndAmountAbove(
        date: Date,
        amount: Float
    ): List<Group>? {
        return groupDao.getGroupsCreatedBeforeAndAmountAbove(date, amount)
    }

    override suspend fun getGroupsCreatedAfterAndAmountBelow(
        date: Date,
        amount: Float
    ): List<Group>? {
        return groupDao.getGroupsCreatedAfterAndAmountBelow(date, amount)
    }

    override suspend fun getGroupsCreatedAfterAndAmountAbove(
        date: Date,
        amount: Float
    ): List<Group>? {
        return groupDao.getGroupsCreatedAfterAndAmountAbove(date, amount)
    }

}