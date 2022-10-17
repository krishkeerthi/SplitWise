package com.example.splitwise.data.local.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitwise.data.local.entity.Group
import java.util.*

@Dao
interface GroupDao {

    // group is a keyword, other examples are order.. so, table names with these names should represented as `table_name`
    @Query("SELECT * FROM `group` WHERE group_name LIKE :name || '%' ORDER BY group_id DESC")   // or append % at the end of name parameter. i.e name = "munar%"
    suspend fun getGroupsStartsWith(name: String): List<Group>?

    @Query("SELECT * FROM `group` WHERE creation_date < :date ORDER BY group_id DESC")
    suspend fun getGroupsCreatedBefore(date: Date): List<Group>?

    @Query("SELECT * FROM `group` WHERE creation_date >= :date ORDER BY group_id DESC")
    suspend fun getGroupsCreatedAfter(date: Date): List<Group>?

    @Query("SELECT * FROM `group` WHERE total_expense < :amount ORDER BY group_id DESC")
    suspend fun getGroupsWithAmountBelow(amount: Float): List<Group>?

    @Query("SELECT * FROM `group` WHERE total_expense >= :amount ORDER BY group_id DESC")
    suspend fun getGroupsWithAmountAbove(amount: Float): List<Group>

    @Query("SELECT * FROM `group` WHERE creation_date < :date AND total_expense < :amount ORDER BY group_id DESC")
    suspend fun getGroupsCreatedBeforeAndAmountBelow(date: Date, amount: Float): List<Group>?

    @Query("SELECT * FROM `group` WHERE creation_date < :date AND total_expense >= :amount ORDER BY group_id DESC")
    suspend fun getGroupsCreatedBeforeAndAmountAbove(date: Date, amount: Float): List<Group>?

    @Query("SELECT * FROM `group` WHERE creation_date >= :date AND total_expense < :amount ORDER BY group_id DESC")
    suspend fun getGroupsCreatedAfterAndAmountBelow(date: Date, amount: Float): List<Group>?

    @Query("SELECT * FROM `group` WHERE creation_date >= :date AND total_expense >= :amount ORDER BY group_id DESC")
    suspend fun getGroupsCreatedAfterAndAmountAbove(date: Date, amount: Float): List<Group>?

    @Query("SELECT * FROM `group` ORDER BY group_id DESC")
    suspend fun getGroups(): List<Group>?

    @Query("SELECT * FROM `group` WHERE group_id IN (:groupIds) ORDER BY group_id DESC")
    suspend fun getGroups(groupIds: List<Int>): List<Group>?

    @Query("SELECT * FROM `group` WHERE group_id = :groupId")
    suspend fun getGroup(groupId: Int): Group?

    @Query("SELECT group_name FROM `group` ")
    suspend fun getGroupNames(): List<String>?

    @Query("UPDATE `group` SET total_expense = :amount WHERE group_id = :groupId")
    suspend fun updateTotalExpense(groupId: Int, amount: Float)

    @Query("UPDATE `group` SET last_active_date = :date WHERE group_id = :groupId")
    suspend fun updateLastActiveDate(groupId: Int, date: Date)

    @Query("SELECT total_expense FROM `group` WHERE group_id = :groupId")
    suspend fun getTotalExpense(groupId: Int): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: Group): Long

    @Query("DELETE FROM `group` WHERE group_id = :groupId")
    suspend fun deleteGroup(groupId: Int)

    @Query("SELECT * FROM `group` WHERE group_name LIKE '%' || :query || '%' ORDER BY group_id DESC")
    suspend fun getGroupsContains(query: String): List<Group>?

    @Query("UPDATE `group` SET group_icon = :uri WHERE group_id = :groupId ")
    suspend fun updateGroupIcon(groupId: Int, uri: Uri)

    @Query("UPDATE `group` SET group_name = :groupName WHERE group_id = :groupId")
    suspend fun updateGroupName(groupId: Int, groupName: String)

    @Query("UPDATE `group` SET group_icon = :groupIcon WHERE group_id = :groupId")
    suspend fun removeGroupIcon(groupId: Int, groupIcon: Uri? = null)

}