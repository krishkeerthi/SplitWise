package com.example.splitwise.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.example.splitwise.data.local.entity.Member
import java.util.*


data class ExpenseMember(
    val expenseId: Int,
    val groupId: Int,
    val expenseName: String,
    val category: Int,
    val totalAmount: Float,
    val splitAmount: Float,
    val payer: Int,
    val payerInfo: Member,
    val date: Date
)