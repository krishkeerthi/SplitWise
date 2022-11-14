package com.example.androidcore.domain.member

import android.net.Uri

data class MemberModel(
    val memberId: Int,
    val name: String,
    val phone: Long,
    val memberProfile: Uri?
)
