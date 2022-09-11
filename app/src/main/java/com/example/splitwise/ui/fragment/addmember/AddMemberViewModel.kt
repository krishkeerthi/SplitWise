package com.example.splitwise.ui.fragment.addmember

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.MemberRepository
import kotlinx.coroutines.launch

class AddMemberViewModel(
    context: Context
) : ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)

    var memberProfile: Uri? = null

    fun checkMember(groupId: Int, member: Member, gotoCreateEditGroupFragment: (Member) -> Unit,  memberExists: () -> Unit,
                    error: () -> Unit
    ) {
        viewModelScope.launch {
            val memberExist = memberRepository.checkMemberExistence(member.name, member.phone)

            if(memberExist){
                memberExists()
            }
            else{
                if(groupId == -1){
                    gotoCreateEditGroupFragment(member)
                }
                else{
                    val memberId = memberRepository.addMember(member.name, member.phone, member.memberProfile)
                    val addedMember = memberRepository.getMember(memberId)

                    if(addedMember != null){
                        memberRepository.addMemberStreak(memberId)
                        gotoCreateEditGroupFragment(addedMember)
                    }
                    else{
                        error()
                    }

                }
            }
        }
    }

//    fun addMemberToGroup(groupId: Int, member: Member, gotoCreateEditGroupFragment: (Member) -> Unit) {
//        if(groupId == -1){
//            gotoCreateEditGroupFragment(member)
//        }
//        else{
//            viewModelScope.launch {
//                val memberId = memberRepository.addMember(member.name, member.phone, member.memberProfile)
//                val member = memberRepository.getMember(memberId)
//
//                gotoCreateEditGroupFragment(member)
//            }
//        }
//    }

}

class AddMemberViewModelFactory(
    private val context: Context
) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddMemberViewModel(context) as T
    }
}