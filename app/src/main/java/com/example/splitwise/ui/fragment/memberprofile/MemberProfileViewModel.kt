package com.example.splitwise.ui.fragment.memberprofile

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.MemberRepository
import kotlinx.coroutines.launch

class MemberProfileViewModel(context: Context, val memberId: Int): ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)

    private val _member = MutableLiveData<Member?>()

    val member: LiveData<Member?>
        get() = _member

    // edit variable
    var editEnabled = false

    var updatedUri: Uri? = null

    fun fetchData(){
        viewModelScope.launch {
            _member.value = memberRepository.getMember(memberId)
        }
    }

    fun updateMember(name: String, phone: Long, gotoCreateEdiGroup: () -> Unit) {
        Log.d(ContentValues.TAG, "onViewCreated: vm update member${name}  ${phone}")
        viewModelScope.launch {
            memberRepository.updateMember(memberId, name, phone)

            // fetch latest data, otherwise member live data will point to the old uri
            fetchData()

            gotoCreateEdiGroup()
        }
    }

    fun updateProfile(uri: Uri) {
        viewModelScope.launch {
            memberRepository.updateMemberProfile(memberId, uri)
        }
    }

    fun newUpdateMember(
        name: String,
        phone: String,
        gotoCreateEditGroupFragment: () -> Unit,
        memberUpdated: () -> Unit
    ) {
//        if ((_member.value!!.name != name) || (_member.value!!.phone.toString() != phone)
//            || (_member.value!!.memberProfile != updatedUri)
//        ) {

            viewModelScope.launch {
                // updating member profile // checking whether uri is updated first, then also checking that uri is different
                // from previous.
                if (_member.value!!.memberProfile != updatedUri) {
                    memberRepository.updateMemberProfile(memberId, updatedUri)
                }

                if((_member.value!!.name != name) || (_member.value!!.phone.toString() != phone)){
                    memberRepository.updateMember(memberId, name, phone.toLong())
                }

                memberUpdated()
                gotoCreateEditGroupFragment()
            }
//        } else {
//           // notEdited()
//        }
    }
}

class MemberProfileViewModelFactory(
    private val context: Context, private val memberId: Int
) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MemberProfileViewModel(context, memberId) as T
    }
}