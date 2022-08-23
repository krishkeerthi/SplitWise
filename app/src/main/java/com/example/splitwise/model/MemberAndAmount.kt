package com.example.splitwise.model

import android.os.Parcelable
import com.example.splitwise.data.local.entity.Member
import kotlinx.parcelize.Parcelize

@Parcelize
data class MemberAndAmount(
    val member: Member,
    val amount: Float
) : Parcelable