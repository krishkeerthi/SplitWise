package com.example.splitwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_expense")
data class GroupExpense(
    @ColumnInfo(name = "group_id")
    val groupId: Int,
    @ColumnInfo(name = "expense_id")
    val expenseId: Int
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "group_expense_id")
    var groupExpenseId: Int = 0
}
