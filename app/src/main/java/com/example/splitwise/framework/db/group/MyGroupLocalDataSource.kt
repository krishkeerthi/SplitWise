package com.example.splitwise.framework.db.group

import android.content.Context
import android.net.Uri
import com.example.androidcore.data.group.MyGroupDataSource
import com.example.androidcore.domain.group.GroupModel
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.dao.GroupDao
import com.example.splitwise.data.local.dao.GroupExpenseDao
import com.example.splitwise.data.local.dao.GroupMemberDao
import com.example.splitwise.data.local.entity.Group
import java.util.*

class MyGroupLocalDataSource(context: Context): MyGroupDataSource {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupDao: GroupDao = database.groupDao()
    private val groupMemberDao: GroupMemberDao = database.groupMemberDao()
    private val groupExpenseDao: GroupExpenseDao = database.groupExpenseDao()

    override suspend fun createGroup(
        name: String,
        description: String,
        date: Date,
        expense: Float,
        icon: Uri?
    ): Int {
        return groupDao.insert(
            Group(
                name,
                description,
                date,
                date,
                expense,
                icon
            )
        ).toInt()
    }

    override suspend fun addGroupMember(groupId: Int, memberId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupMembers(groupId: Int): List<Int>? {
        TODO("Not yet implemented")
    }

    override suspend fun addGroupExpense(groupId: Int, expenseId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun getGroup(groupId: Int): GroupModel? {
        var groupModel: GroupModel? = null
        groupDao.getGroup(groupId)?.let {
            groupModel = GroupModel(
                it.groupId, it.groupName, it.description, it.creationDate, it.lastActiveDate, it.totalExpense, it.groupIcon
            )
        }
        return groupModel
    }

    override suspend fun getGroups(): List<GroupModel>? {
        val groupModels: MutableList<GroupModel> = mutableListOf()
        groupDao.getGroups()?.let { groups ->
            for(group in groups){
                val groupModel = GroupModel(
                    group.groupId, group.groupName, group.description, group.creationDate, group.lastActiveDate,
                    group.totalExpense, group.groupIcon)

                groupModels.add(groupModel)
            }

        }
        return groupModels
    }

    override suspend fun getGroups(groupIds: List<Int>): List<GroupModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupsStartsWith(query: String): List<GroupModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupsContains(query: String): List<GroupModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun updateTotalExpense(groupId: Int, amount: Float) {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalExpense(groupId: Int): Float? {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupsCreatedBefore(date: Date): List<GroupModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupsCreatedAfter(date: Date): List<GroupModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupsWithAmountBelow(amount: Float): List<GroupModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupsWithAmountAbove(amount: Float): List<GroupModel> {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupsCreatedBeforeAndAmountBelow(
        date: Date,
        amount: Float
    ): List<GroupModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupsCreatedBeforeAndAmountAbove(
        date: Date,
        amount: Float
    ): List<GroupModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupsCreatedAfterAndAmountBelow(
        date: Date,
        amount: Float
    ): List<GroupModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupsCreatedAfterAndAmountAbove(
        date: Date,
        amount: Float
    ): List<GroupModel>? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGroup(groupId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun updateGroupIcon(groupId: Int, uri: Uri) {
        TODO("Not yet implemented")
    }

    override suspend fun updateGroupName(groupId: Int, groupName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeGroup(groupId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun removeGroupMembers(groupId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun removeGroupIcon(groupId: Int) {
        TODO("Not yet implemented")
    }
}