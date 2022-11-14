package com.example.androidcore.interactors.group

import com.example.androidcore.data.group.MyGroupRepository
import com.example.androidcore.domain.group.GroupModel

class GetGroup(private val myGroupRepository: MyGroupRepository) {
    suspend operator fun invoke(groupId: Int): GroupModel? {
        return myGroupRepository.getGroup(groupId)
    }
}