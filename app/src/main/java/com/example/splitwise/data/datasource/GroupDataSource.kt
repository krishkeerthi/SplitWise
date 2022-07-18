package com.example.splitwise.data.datasource

import com.example.splitwise.data.local.entity.Group
import java.util.*

interface GroupDataSource {

    suspend fun createGroup(name: String, description: String, date: Date, expense: Float): Int

    suspend fun addGroupMember(groupId: Int, memberId: Int)

    suspend fun getGroupMembers(groupId: Int): List<Int>?

    suspend fun addGroupExpense(groupId: Int, expenseId: Int)

    suspend fun getGroup(groupId: Int): Group?

    suspend fun getGroups(): List<Group>?


}