package com.example.androidcore.interactors.transaction

import com.example.androidcore.data.transaction.MyTransactionRepository
import com.example.androidcore.domain.transaction.MemberPaymentStatsModel

class TransactionStatsInGroup(private val myTransactionRepository: MyTransactionRepository) {
    suspend operator fun invoke(groupId: Int): List<MemberPaymentStatsModel>? {
        return myTransactionRepository.transactionStats(groupId)
    }
}