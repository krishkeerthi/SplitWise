package com.example.androidcore.interactors.transaction

import com.example.androidcore.data.transaction.MyTransactionRepository
import com.example.androidcore.domain.transaction.MemberPaymentStatsModel

class TransactionStatsInGroups(private val myTransactionRepository: MyTransactionRepository) {
    suspend operator fun invoke(groupIds: List<Int>): List<MemberPaymentStatsModel>? {
        return myTransactionRepository.transactionStats(groupIds)
    }
}