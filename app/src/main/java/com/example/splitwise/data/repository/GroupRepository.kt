package com.example.splitwise.data.repository

import android.content.Context
import com.example.splitwise.data.datasource.GroupDataSource
import com.example.splitwise.data.datasource.MemberDataSource
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.localdatasource.GroupLocalDataSource
import com.example.splitwise.data.local.localdatasource.MemberLocalDataSource
import com.example.splitwise.util.titleCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        return withContext(Dispatchers.IO){dataSource.createGroup(name.titleCase(), description.titleCase(), date, expense)}
    }

    suspend fun addGroupMember(groupId: Int, memberId: Int){
        dataSource.addGroupMember(groupId, memberId)
    }

    suspend fun addGroupExpense(groupId: Int, expenseId: Int){
        dataSource.addGroupExpense(groupId, expenseId)
    }

    suspend fun getGroup(groupId: Int): Group?{
        return withContext(Dispatchers.IO){dataSource.getGroup(groupId)}
    }

    suspend fun getGroups(): List<Group>?{
        return withContext(Dispatchers.IO){dataSource.getGroups()}
    }

    suspend fun getGroups(groupIds: List<Int>): List<Group>?{
        return withContext(Dispatchers.IO){dataSource.getGroups(groupIds)}
    }

    suspend fun getGroupsStartsWith(query: String): List<Group>?{
        return if(query == "") null else withContext(Dispatchers.IO){dataSource.getGroupsStartsWith(query)}
    }

    suspend fun getGroupMembers(groupId: Int): List<Int>?{
        return withContext(Dispatchers.IO){dataSource.getGroupMembers(groupId)}
    }

    suspend fun getTotalExpense(groupId: Int): Float?{
        return withContext(Dispatchers.IO){dataSource.getTotalExpense(groupId)}
    }

    suspend fun updateTotalExpense(groupId: Int, amount: Float){
        CoroutineScope(Dispatchers.IO).launch{
            getTotalExpense(groupId)?.let {
                dataSource.updateTotalExpense(groupId, it + amount)
            }
        }.join()
    }

    suspend fun getGroupsCreatedBefore(date: Date): List<Group>? = withContext(Dispatchers.IO){
        dataSource.getGroupsCreatedBefore(date)
    }

    suspend fun getGroupsCreatedAfter(date: Date): List<Group>? = withContext(Dispatchers.IO){
        dataSource.getGroupsCreatedAfter(date)
    }

    suspend fun getGroupsWithAmountBelow(amount: Float): List<Group>? = withContext(Dispatchers.IO){
        dataSource.getGroupsWithAmountBelow(amount)
    }

    suspend fun getGroupsWithAmountAbove(amount: Float): List<Group> = withContext(Dispatchers.IO){
        dataSource.getGroupsWithAmountAbove(amount)
    }

    suspend fun getGroupsCreatedBeforeAndAmountBelow(date: Date, amount: Float): List<Group>? = withContext(Dispatchers.IO){
        dataSource.getGroupsCreatedBeforeAndAmountBelow(date, amount)
    }

    suspend fun getGroupsCreatedBeforeAndAmountAbove(date: Date, amount: Float): List<Group>? = withContext(Dispatchers.IO){
        dataSource.getGroupsCreatedBeforeAndAmountAbove(date, amount)
    }

    suspend fun getGroupsCreatedAfterAndAmountBelow(date: Date, amount: Float): List<Group>? = withContext(Dispatchers.IO){
        dataSource.getGroupsCreatedAfterAndAmountBelow(date, amount)
    }

    suspend fun getGroupsCreatedAfterAndAmountAbove(date: Date, amount: Float): List<Group>? = withContext(Dispatchers.IO){
        dataSource.getGroupsCreatedAfterAndAmountAbove(date, amount)
    }

    suspend fun updateLastActiveDate(date: Date){

    }

    suspend fun deleteGroup(groupId: Int) {
        withContext(Dispatchers.IO){
            dataSource.deleteGroup(groupId)
        }
    }

    suspend fun getGroupsContain(query: String): List<Group>? {
        return if(query == "") null else withContext(Dispatchers.IO){dataSource.getGroupsContains(query)}
    }
}