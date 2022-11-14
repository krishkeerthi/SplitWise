package com.example.splitwise.framework

import com.example.androidcore.interactors.group.GroupInteractors
import com.example.androidcore.interactors.member.MemberInteractors
import com.example.androidcore.interactors.transaction.TransactionInteractors

data class Interactors(
    val groupInteractors: GroupInteractors,

    val memberInteractors: MemberInteractors,

    val transactionInteractors: TransactionInteractors
)
