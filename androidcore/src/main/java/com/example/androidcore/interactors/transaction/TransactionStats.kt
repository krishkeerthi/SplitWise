package com.example.androidcore.interactors.transaction

import com.example.androidcore.data.transaction.MyTransactionRepository
import com.example.androidcore.domain.transaction.MemberPaymentStatsModel

class TransactionStats(private val myTransactionRepository: MyTransactionRepository) {
    suspend operator fun invoke(): List<MemberPaymentStatsModel>? {
        return myTransactionRepository.transactionStats()
    }
}