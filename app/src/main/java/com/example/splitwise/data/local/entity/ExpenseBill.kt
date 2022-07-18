package com.example.splitwise.data.local.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_bill")
data class ExpenseBill(
    @ColumnInfo(name = "expense_id")
    val expenseId: Int,
    val bill: Uri,  // Image URI
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "expense_bill_id")
    var expenseBillId: Int = 0
}
