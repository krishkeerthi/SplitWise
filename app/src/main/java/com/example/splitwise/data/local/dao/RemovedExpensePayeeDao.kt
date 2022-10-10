package com.example.splitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitwise.data.local.entity.RemovedExpensePayee

@Dao
interface RemovedExpensePayeeDao {

    @Query("SELECT payee_id FROM removed_expense_payee WHERE expense_id= :expenseId")
    suspend fun getPayees(expenseId: Int): List<Int>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expensePayees: RemovedExpensePayee)

    @Query("DELETE FROM removed_expense_payee WHERE expense_id = :expenseId AND payee_id= :payeeId")
    suspend fun removePayee(expenseId: Int, payeeId: Int)
}