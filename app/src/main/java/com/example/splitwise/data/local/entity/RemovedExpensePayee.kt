package com.example.splitwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "removed_expense_payee")
data class RemovedExpensePayee(
    @ColumnInfo(name = "expense_id")
    val expenseId: Int,
    @ColumnInfo(name = "payee_id")
    val payeeId: Int,
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "removed_expense_payee_id")
    var removedExpensePayeeId: Int = 0
}