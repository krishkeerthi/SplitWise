package com.example.androidcore.interactors.group

import com.example.androidcore.data.group.MyGroupRepository
import com.example.androidcore.domain.group.GroupModel

class GetGroups(private val myGroupRepository: MyGroupRepository) {
    suspend operator fun invoke(): List<GroupModel>? {
        return myGroupRepository.getGroups()
    }
}