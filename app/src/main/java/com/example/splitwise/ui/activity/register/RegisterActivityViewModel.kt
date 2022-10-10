package com.example.splitwise.ui.activity.register

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.repository.MemberRepository
import kotlinx.coroutines.launch

class RegisterActivityViewModel(context: Context) : ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)

    private val _memberId = MutableLiveData<Int?>()

    val memberId: LiveData<Int?>
        get() = _memberId

    fun registerMember(name: String, phone: Long) {

        viewModelScope.launch {
            val memberId = memberRepository.addMember("$name(You)", phone, getProfileUri(name))
            memberRepository.addMemberStreak(memberId)

            _memberId.value = memberId
        }
    }

    private fun getProfileUri(name: String): Uri?{
        val uri = when{
            name.lowercase().contains("ree") -> Uri.parse("android.resource://com.example.splitwise/drawable/reeganth")
            name.lowercase().contains("vis") -> Uri.parse("android.resource://com.example.splitwise/drawable/visalakshi")
            name.lowercase().contains("kee") -> Uri.parse("android.resource://com.example.splitwise/drawable/keerthi")
            else -> null
        }
        return uri
    }
}

class RegisterActivityViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterActivityViewModel(context) as T
    }
}