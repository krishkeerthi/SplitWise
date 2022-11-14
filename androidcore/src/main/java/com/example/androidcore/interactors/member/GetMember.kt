package com.example.androidcore.interactors.member

import com.example.androidcore.data.member.MyMemberRepository
import com.example.androidcore.domain.member.MemberModel

class GetMember(private val myMemberRepository: MyMemberRepository) {
    suspend operator fun invoke(memberId: Int): MemberModel? {
        return myMemberRepository.getMember(memberId)
    }
}