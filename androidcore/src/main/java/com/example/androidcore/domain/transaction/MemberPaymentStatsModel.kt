package com.example.androidcore.domain.transaction

data class MemberPaymentStatsModel(
    val memberId: Int,
    val amountLend: Float,
    val amountOwed: Float
)
