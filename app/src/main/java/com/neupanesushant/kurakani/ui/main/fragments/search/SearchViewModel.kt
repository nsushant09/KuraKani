package com.neupanesushant.kurakani.ui.main.fragments.search

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neupanesushant.kurakani.data.UserManager
import com.neupanesushant.kurakani.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchViewModel() : ViewModel(), KoinComponent {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    private val _allUsers = MutableLiveData<List<User>>()
    val allUser get() = _allUsers
    val tempUsers = arrayListOf<User>()

    private val _searchedList = MutableLiveData<List<User>>()
    val searchedList get() = _searchedList

    private val userManager: UserManager by inject()


    init {
        getAllUsersFromDatabase()
        viewModelScope.launch {
            userManager.users.collectLatest {
                if (it == null) return@collectLatest
                tempUsers.add(it)
                _allUsers.postValue(tempUsers)
            }
        }
    }

    private fun getAllUsersFromDatabase() {
        uiScope.launch {
            userManager.getAllUser()
        }
    }

    fun filterSearch(searchedText: String) {
        val tempList = ArrayList<User>()
        _allUsers.value?.forEach {
            if (isStringInName(it.fullName, searchedText)) {
                tempList.add(it)
            }
            _searchedList.value = tempList
        }

    }

    private fun isStringInName(name: String?, target: String): Boolean {
        val lengthOfTarget = target.length
        var lengthOfName = name?.length
        if (lengthOfName == null) {
            lengthOfName = 0
        }
        val loopSize = lengthOfName - lengthOfTarget
        for (i in 0 until loopSize + 1) {
            try {
                if (name?.substring(i, lengthOfTarget + i).equals(target, true)) {
                    return true
                }
            } catch (e: Exception) {
                continue
            }
        }
        return false

    }
}