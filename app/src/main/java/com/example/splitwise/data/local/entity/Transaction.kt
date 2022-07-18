package com.example.splitwise.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Transaction(
    @ColumnInfo(name = "group_id")
    val groupId: Int,
    val payer: Int,
    val payee: Int,
    val amount: Float
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    var transactionId: Int = 0
}
