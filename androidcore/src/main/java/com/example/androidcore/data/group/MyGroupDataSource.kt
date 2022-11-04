package com.example.androidcore.data.group

import android.net.Uri
import java.util.*
import com.example.androidcore.domain.group.GroupModel

interface MyGroupDataSource {
    suspend fun createGroup(name: String, description: String, date: Date, expense: Float, icon: Uri?): Int

    suspend fun addGroupMember(groupId: Int, memberId: Int)

    suspend fun getGroupMembers(groupId: Int): List<Int>?

    suspend fun addGroupExpense(groupId: Int, expenseId: Int)

    suspend fun getGroup(groupId: Int): GroupModel?

    suspend fun getGroups(): List<GroupModel>?

    suspend fun getGroups(groupIds: List<Int>): List<GroupModel>?

    suspend fun getGroupsStartsWith(query: String): List<GroupModel>?

    suspend fun getGroupsContains(query: String): List<GroupModel>?

    suspend fun updateTotalExpense(groupId: Int, amount: Float)

    suspend fun getTotalExpense(groupId: Int): Float?

    suspend fun getGroupsCreatedBefore(date: Date): List<GroupModel>?

    suspend fun getGroupsCreatedAfter(date: Date): List<GroupModel>?

    suspend fun getGroupsWithAmountBelow(amount: Float): List<GroupModel>?

    suspend fun getGroupsWithAmountAbove(amount: Float): List<GroupModel>

    suspend fun getGroupsCreatedBeforeAndAmountBelow(date: Date, amount: Float): List<GroupModel>?

    suspend fun getGroupsCreatedBeforeAndAmountAbove(date: Date, amount: Float): List<GroupModel>?

    suspend fun getGroupsCreatedAfterAndAmountBelow(date: Date, amount: Float): List<GroupModel>?

    suspend fun getGroupsCreatedAfterAndAmountAbove(date: Date, amount: Float): List<GroupModel>?

    suspend fun deleteGroup(groupId: Int)

    suspend fun updateGroupIcon(groupId: Int, uri: Uri)

    suspend fun updateGroupName(groupId: Int, groupName: String)

    suspend fun removeGroup(groupId: Int)

    suspend fun removeGroupMembers(groupId: Int)

    suspend fun removeGroupIcon(groupId: Int)
}