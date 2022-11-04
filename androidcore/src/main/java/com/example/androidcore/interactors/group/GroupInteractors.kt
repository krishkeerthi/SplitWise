package com.example.androidcore.interactors.group

import com.example.androidcore.data.group.MyGroupRepository

class GroupInteractors(
    myGroupRepository: MyGroupRepository
){
    val createGroup: CreateGroup = CreateGroup(myGroupRepository)

}
