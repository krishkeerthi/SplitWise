package com.example.splitwise.model

data class MemberPaymentStats(
    val memberId: Int,
    val amountLend: Float,
    val amountOwed: Float
)
