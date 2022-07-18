package com.example.splitwise.data.repository

import android.content.Context
import com.example.splitwise.data.datasource.GroupDataSource
import com.example.splitwise.data.datasource.MemberDataSource
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.localdatasource.GroupLocalDataSource
import com.example.splitwise.data.local.localdatasource.MemberLocalDataSource
import java.util.*

class GroupRepository(
    private val database: SplitWiseRoomDatabase
) {

    private var dataSource: GroupDataSource = GroupLocalDataSource(
        database.groupDao(),
        database.groupMemberDao(),
        database.groupExpenseDao()
    )

    suspend fun createGroup(name: String, description: String, date: Date, expense: Float): Int{
        return dataSource.createGroup(name, description, date, expense)
    }

    suspend fun addGroupMember(groupId: Int, memberId: Int){
        dataSource.addGroupMember(groupId, memberId)
    }

    suspend fun addGroupExpense(groupId: Int, expenseId: Int){
        dataSource.addGroupExpense(groupId, expenseId)
    }

    suspend fun getGroup(groupId: Int): Group?{
        return dataSource.getGroup(groupId)
    }

    suspend fun getGroups(): List<Group>?{
        return dataSource.getGroups()
    }

    suspend fun getGroupMembers(groupId: Int): List<Int>?{
        return dataSource.getGroupMembers(groupId)
    }

    suspend fun updateLastActiveDate(date: Date){

    }
}