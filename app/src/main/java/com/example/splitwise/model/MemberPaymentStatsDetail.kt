package com.example.splitwise.model

data class MemberPaymentStatsDetail(
    val memberId: Int,
    val memberName: String,
    val amountLend: Float,
    val amountOwed: Float
)

