package com.example.splitwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Expense(
    @ColumnInfo(name = "group_id")
    val groupId: Int,
    @ColumnInfo(name = "expense_name")
    val expenseName: String,
    val category: Int,
    @ColumnInfo(name = "total_amount")
    val totalAmount: Float,
    @ColumnInfo(name = "split_amount")
    val splitAmount: Float,
    val payer: Int,
    val date: Date
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "expense_id")
    var expenseId: Int = 0
}
