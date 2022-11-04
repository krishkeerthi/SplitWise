package com.example.androidcore.data.group

import android.net.Uri
import com.example.androidcore.domain.group.GroupModel
import com.example.androidcore.util.titleCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MyGroupRepository(private val dataSource: MyGroupDataSource) {

    suspend fun createGroup(name: String, description: String, date: Date, expense: Float, icon: Uri?): Int{
        return withContext(Dispatchers.IO){dataSource.createGroup(name.titleCase(), description.titleCase(), date, expense, icon)}
    }

    suspend fun addGroupMember(groupId: Int, memberId: Int){
        dataSource.addGroupMember(groupId, memberId)
    }

    suspend fun addGroupExpense(groupId: Int, expenseId: Int){
        dataSource.addGroupExpense(groupId, expenseId)
    }

    suspend fun getGroup(groupId: Int): GroupModel?{
        return withContext(Dispatchers.IO){dataSource.getGroup(groupId)}
    }

    suspend fun getGroups(): List<GroupModel>?{
        return withContext(Dispatchers.IO){dataSource.getGroups()}
    }

    suspend fun getGroups(groupIds: List<Int>): List<GroupModel>?{
        return withContext(Dispatchers.IO){dataSource.getGroups(groupIds)}
    }

    suspend fun getGroupsStartsWith(query: String): List<GroupModel>?{
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

    suspend fun reduceTotalExpense(groupId: Int, amount: Float){
        withContext(Dispatchers.IO){
            getTotalExpense(groupId)?.let {
                dataSource.updateTotalExpense(groupId,  amount)
            }
        }
    }

    suspend fun getGroupsCreatedBefore(date: Date): List<GroupModel>? = withContext(Dispatchers.IO){
        dataSource.getGroupsCreatedBefore(date)
    }

    suspend fun getGroupsCreatedAfter(date: Date): List<GroupModel>? = withContext(Dispatchers.IO){
        dataSource.getGroupsCreatedAfter(date)
    }

    suspend fun getGroupsWithAmountBelow(amount: Float): List<GroupModel>? = withContext(Dispatchers.IO){
        dataSource.getGroupsWithAmountBelow(amount)
    }

    suspend fun getGroupsWithAmountAbove(amount: Float): List<GroupModel> = withContext(Dispatchers.IO){
        dataSource.getGroupsWithAmountAbove(amount)
    }

    suspend fun getGroupsCreatedBeforeAndAmountBelow(date: Date, amount: Float): List<GroupModel>? = withContext(
        Dispatchers.IO){
        dataSource.getGroupsCreatedBeforeAndAmountBelow(date, amount)
    }

    suspend fun getGroupsCreatedBeforeAndAmountAbove(date: Date, amount: Float): List<GroupModel>? = withContext(
        Dispatchers.IO){
        dataSource.getGroupsCreatedBeforeAndAmountAbove(date, amount)
    }

    suspend fun getGroupsCreatedAfterAndAmountBelow(date: Date, amount: Float): List<GroupModel>? = withContext(
        Dispatchers.IO){
        dataSource.getGroupsCreatedAfterAndAmountBelow(date, amount)
    }

    suspend fun getGroupsCreatedAfterAndAmountAbove(date: Date, amount: Float): List<GroupModel>? = withContext(
        Dispatchers.IO){
        dataSource.getGroupsCreatedAfterAndAmountAbove(date, amount)
    }

    suspend fun updateLastActiveDate(date: Date){

    }

    suspend fun deleteGroup(groupId: Int) {
        withContext(Dispatchers.IO){
            dataSource.deleteGroup(groupId)
        }
    }

    suspend fun getGroupsContain(query: String): List<GroupModel>? {
        return if(query == "") null else withContext(Dispatchers.IO){dataSource.getGroupsContains(query)}
    }

    suspend fun updateGroupIcon(groupId: Int, uri: Uri) {
        withContext(Dispatchers.IO){
            dataSource.updateGroupIcon(groupId, uri)
        }
    }

    suspend fun updateGroupName(groupId: Int, groupName: String) {
        withContext(Dispatchers.IO){
            dataSource.updateGroupName(groupId, groupName)
        }
    }

    suspend fun removeGroup(groupId: Int) {
        withContext(Dispatchers.IO){
            dataSource.removeGroup(groupId)
        }
    }

    suspend fun removeGroupMembers(groupId: Int) {
        withContext(Dispatchers.IO){
            dataSource.removeGroupMembers(groupId)
        }
    }

    suspend fun removeGroupIcon(groupId: Int) {
        withContext(Dispatchers.IO){
            dataSource.removeGroupIcon(groupId)
        }
    }
}