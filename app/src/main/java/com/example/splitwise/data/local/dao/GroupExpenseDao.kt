package com.example.splitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitwise.data.local.entity.GroupExpense

@Dao
interface GroupExpenseDao {
    @Query("SELECT expense_id FROM group_expense WHERE group_id= :groupId")
    suspend fun getExpenses(groupId: Int): List<Int>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(groupExpense: GroupExpense)
}