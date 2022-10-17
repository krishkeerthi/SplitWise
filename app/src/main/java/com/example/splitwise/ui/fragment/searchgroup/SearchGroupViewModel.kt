package com.example.splitwise.ui.fragment.searchgroup

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.repository.GroupRepository
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

class SearchGroupViewModel(context: Context) : ViewModel() {
    private val groupsRepository = GroupRepository(SplitWiseRoomDatabase.getInstance(context))

    var textEntered: String = ""

    private val _groups = MutableLiveData<List<Group>?>()

    val groups: LiveData<List<Group>?>
        get() = _groups

    fun fetchData() {
        Log.d(
            ContentValues.TAG,
            "groupsviewmodel:search fetch called with textentered is $textEntered"
        )

        viewModelScope.launch {
            if(textEntered == ""){
                _groups.value = groupsRepository.getGroups()
            }
            else {
                val groupsStartedWith = groupsRepository.getGroupsStartsWith(textEntered)
                val groupsContained = groupsRepository.getGroupsContain(textEntered)

                _groups.value = merge(groupsStartedWith, groupsContained)
            }
        }
    }

    private fun merge(startedWithList: List<Group>?, containsList: List<Group>?): List<Group>?{
        var mergedList= mutableListOf<Group>()

        startedWithList?.let {
            for(group in it)
                mergedList.add(group)
        }

        containsList?.let {
            for(group in it){
                if(!mergedList.contains(group))
                    mergedList.add(group)
            }

        }

        return mergedList.ifEmpty {
            if(textEntered == "") null
        else listOf<Group>()}
    }
}

class SearchGroupViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchGroupViewModel(context) as T
    }
}