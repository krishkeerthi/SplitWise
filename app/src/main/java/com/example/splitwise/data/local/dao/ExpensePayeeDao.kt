package com.example.splitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitwise.data.local.entity.ExpensePayee

@Dao
interface ExpensePayeeDao {

    @Query("SELECT payee_id FROM expense_payee WHERE expense_id= :expenseId")
    suspend fun getPayees(expenseId: Int): List<Int>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expensePayees: ExpensePayee)
}