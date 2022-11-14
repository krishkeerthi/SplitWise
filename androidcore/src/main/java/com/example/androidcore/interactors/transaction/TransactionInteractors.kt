package com.example.androidcore.interactors.transaction

import com.example.androidcore.data.transaction.MyTransactionRepository

class TransactionInteractors(myTransactionRepository: MyTransactionRepository) {
    val transactionStats: TransactionStats = TransactionStats(myTransactionRepository)

    val transactionStatsInGroup: TransactionStatsInGroup = TransactionStatsInGroup(myTransactionRepository)

    val transactionStatsInGroups: TransactionStatsInGroups = TransactionStatsInGroups(myTransactionRepository)

}