package com.example.splitwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_payee")
data class ExpensePayee(
    @ColumnInfo(name = "expense_id")
    val expenseId: Int,
    @ColumnInfo(name = "payee_id")
    val payeeId: Int,
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "expense_payee_id")
    var expensePayeeId: Int = 0
}
