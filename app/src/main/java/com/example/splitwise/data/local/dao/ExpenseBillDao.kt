package com.example.splitwise.data.local.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitwise.data.local.entity.ExpenseBill

@Dao
interface ExpenseBillDao {

    @Query("SELECT bill FROM expense_bill WHERE expense_id= :expenseId")
    suspend fun getBills(expenseId: Int): List<Uri>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expenseBills: ExpenseBill)
}