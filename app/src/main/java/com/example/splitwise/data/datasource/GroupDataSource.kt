package com.example.splitwise.data.datasource

import androidx.room.Query
import com.example.splitwise.data.local.entity.Group
import java.util.*

interface GroupDataSource {

    suspend fun createGroup(name: String, description: String, date: Date, expense: Float): Int

    suspend fun addGroupMember(groupId: Int, memberId: Int)

    suspend fun getGroupMembers(groupId: Int): List<Int>?

    suspend fun addGroupExpense(groupId: Int, expenseId: Int)

    suspend fun getGroup(groupId: Int): Group?

    suspend fun getGroups(): List<Group>?

    suspend fun getGroupsStartsWith(query: String): List<Group>?

    suspend fun updateTotalExpense(groupId: Int, amount: Float)

    suspend fun getTotalExpense(groupId: Int): Float?

    suspend fun getGroupsCreatedBefore(date: Date): List<Group>?

    suspend fun getGroupsCreatedAfter(date: Date): List<Group>?

    suspend fun getGroupsWithAmountBelow(amount: Float): List<Group>?

    suspend fun getGroupsWithAmountAbove(amount: Float): List<Group>

    suspend fun getGroupsCreatedBeforeAndAmountBelow(date: Date, amount: Float): List<Group>?

    suspend fun getGroupsCreatedBeforeAndAmountAbove(date: Date, amount: Float): List<Group>?

    suspend fun getGroupsCreatedAfterAndAmountBelow(date: Date, amount: Float): List<Group>?

    suspend fun getGroupsCreatedAfterAndAmountAbove(date: Date, amount: Float): List<Group>?

    suspend fun deleteGroup(groupId: Int)
}