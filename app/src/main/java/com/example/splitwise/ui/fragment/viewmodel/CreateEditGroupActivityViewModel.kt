package com.example.splitwise.ui.fragment.viewmodel

import androidx.lifecycle.ViewModel
import com.example.splitwise.data.local.entity.Member

class CreateEditGroupActivityViewModel: ViewModel() {

    var selectedMembers = listOf<Member>()

}