package com.example.splitwise.data.local.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitwise.data.local.entity.ExpenseBill
import com.example.splitwise.model.BillUri

@Dao
interface ExpenseBillDao {

    @Query("SELECT bill FROM expense_bill WHERE expense_id= :expenseId")
    suspend fun getBills(expenseId: Int): List<Uri>?

    @Query("SELECT expense_bill_id as id, bill as uri FROM expense_bill WHERE expense_id= :expenseId")
    suspend fun getBillsWithId(expenseId: Int): List<BillUri>?

    @Query("DELETE FROM expense_bill WHERE expense_bill_id= :billId")
    suspend fun deleteBill(billId: Int): Unit

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expenseBills: ExpenseBill)

}