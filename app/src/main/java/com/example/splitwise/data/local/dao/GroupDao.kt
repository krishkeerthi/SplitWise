package com.example.splitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitwise.data.local.entity.Group
import java.util.*

@Dao
interface GroupDao {

    // group is a keyword, other examples are order.. so, table names with these names should represented as `table_name`
    @Query("SELECT * FROM `group` WHERE group_name LIKE :name || '%'")   // or append % at the end of name parameter. i.e name = "munar%"
    suspend fun getGroupsStartsWith(name: String): List<Group>?

    @Query("SELECT * FROM `group` WHERE creation_date < :date")
    suspend fun getGroupsCreatedBefore(date: Date): List<Group>?

    @Query("SELECT * FROM `group` WHERE creation_date >= :date")
    suspend fun getGroupsCreatedAfter(date: Date): List<Group>?

    @Query("SELECT * FROM `group` WHERE total_expense < :expense")
    suspend fun getGroupsWithExpenseBelow(expense: Float): List<Group>?

    @Query("SELECT * FROM `group` WHERE total_expense >= :expense")
    suspend fun getGroupsWithExpenseAbove(expense: Float): List<Group>

    @Query("SELECT * FROM `group`")
    suspend fun getGroups(): List<Group>?

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


}