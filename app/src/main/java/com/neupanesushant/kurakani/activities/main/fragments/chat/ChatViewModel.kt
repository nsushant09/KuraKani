package com.neupanesushant.kurakani.activities.main.fragments.chat

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.data.FirebaseInstance
import com.neupanesushant.kurakani.data.MessageManager
import com.neupanesushant.kurakani.data.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class ChatViewModel(private val application: Application) : ViewModel(),
    KoinComponent {


    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> get() = _allUsers

    private val _isAllUILoaded = MutableLiveData<Boolean>()
    val isAllUILoaded: LiveData<Boolean> get() = _isAllUILoaded

    private val _latestMessages = MutableLiveData<ArrayList<Message>>()
    val latestMessages: LiveData<ArrayList<Message>> get() = _latestMessages

    private val _usersOfLatestMessages = MutableLiveData<ArrayList<User>>()
    val usersOfLatestMessages: LiveData<ArrayList<User>> get() = _usersOfLatestMessages

    private val userManager: UserManager by inject()
    private val messageManager: MessageManager by inject { parametersOf("") }

    private val _userOfLatestMessageLoaded = MutableLiveData<Boolean>()
    private val _allUsersLoaded = MutableLiveData<Boolean>()


    init {
        _isAllUILoaded.value = false
        _allUsersLoaded.value = false
        _userOfLatestMessageLoaded.value = false

        getAllUsersFromDatabase()
        getLatestMessages()

        viewModelScope.launch {
            messageManager.latestMessages.collectLatest {
                _latestMessages.postValue(it)
                getUsersOfLatestMessages()
            }
        }

        viewModelScope.launch {
            userManager.allUsers.collectLatest {
                _allUsers.postValue(it)
                _allUsersLoaded.value = true
                validateUILoaded()
            }
        }
    }

    private fun validateUILoaded() {
        if (_userOfLatestMessageLoaded.value!! && _allUsersLoaded.value!!) {
            setIsUILoaded(true)
        }
    }

    private fun getAllUsersFromDatabase() {
        uiScope.launch {
            userManager.getAllUser()
        }
    }

    private fun getLatestMessages() {
        uiScope.launch {
            messageManager.getLatestMessage()
        }
    }

    private fun getUsersOfLatestMessages() {
        val tempUsersOfLatestMessage = arrayListOf<User>()
        uiScope.launch {
            _latestMessages.value?.forEach {

                val uid = if (it.fromUid == FirebaseInstance.fromId) {
                    it.toUid ?: ""
                } else {
                    it.fromUid ?: ""
                }
                userManager.getSelectedUser(uid) { user ->
                    tempUsersOfLatestMessage.add(user)
                    if (tempUsersOfLatestMessage.size == _latestMessages.value!!.size) {
                        _usersOfLatestMessages.value = tempUsersOfLatestMessage
                    }
                }
            }

            if (_latestMessages.value?.size == 0) {
                _usersOfLatestMessages.value = arrayListOf()
            }
            sortLatestMessages()
        }

    }

    private fun sortLatestMessages() {


        val tempUser: ArrayList<User> = _usersOfLatestMessages.value!!
        val tempMessage: ArrayList<Message> = _latestMessages.value!!
        for (i in 0 until tempUser.size - 1) {
            for (j in i until tempUser.size) {
                if (tempMessage[j].timeStamp!! > tempMessage[i].timeStamp!!) {
                    val mTemp = tempMessage[i]
                    tempMessage[i] = tempMessage[j]
                    tempMessage[j] = mTemp

                    val uTemp = tempUser[i]
                    tempUser[i] = tempUser[j]
                    tempUser[j] = uTemp
                }
            }
        }

        _usersOfLatestMessages.value = tempUser
        _latestMessages.value = tempMessage
        _userOfLatestMessageLoaded.value = true
        validateUILoaded()
    }

    fun setIsUILoaded(boolean: Boolean) {
        _isAllUILoaded.value = boolean
    }


}


