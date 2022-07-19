package com.example.splitwise.ui.activity.register

import android.content.Context
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.repository.MemberRepository
import kotlinx.coroutines.launch

class RegisterActivityViewModel(context: Context) : ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val memberRepository = MemberRepository(database)

    private val _registered = MutableLiveData<Boolean>()

    val registered: LiveData<Boolean>
        get() = _registered

    init {
        _registered.value = false
    }

    fun registerMember(name: String, phone: Long) {
        viewModelScope.launch {
            memberRepository.addMember(
                name,
                phone
            ) // May be we can check the success status with returned value and then set registerd to true
            _registered.value = true
        }
    }
}

class RegisterActivityViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterActivityViewModel(context) as T
    }
}