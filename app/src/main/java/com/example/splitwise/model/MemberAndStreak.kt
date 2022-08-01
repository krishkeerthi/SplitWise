package com.example.splitwise.model

import com.example.splitwise.data.local.entity.Member

data class MemberAndStreak(
    val member: Member,
    val streak: Int
)
