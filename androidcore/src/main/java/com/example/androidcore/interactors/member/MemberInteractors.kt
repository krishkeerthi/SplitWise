package com.example.androidcore.interactors.member

import com.example.androidcore.data.member.MyMemberRepository

class MemberInteractors(myMemberRepository: MyMemberRepository) {
    val getMember: GetMember = GetMember(myMemberRepository)
}