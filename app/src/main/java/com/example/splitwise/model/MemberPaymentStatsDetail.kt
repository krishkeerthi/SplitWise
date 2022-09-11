package com.example.splitwise.model

import android.net.Uri

data class MemberPaymentStatsDetail(
    val memberId: Int,
    val memberName: String,
    val memberProfile: Uri?,
    val amountLend: Float,
    val amountOwed: Float
)

