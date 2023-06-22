package com.neupanesushant.kurakani.activities.main.fragments.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.data.FirebaseInstance
import com.neupanesushant.kurakani.data.MessageManager
import com.neupanesushant.kurakani.data.UserManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class ChatViewModel(private val application: Application) : ViewModel(), FirebaseInstance,
    KoinComponent {


    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> get() = _allUsers

    private val _isAllUILoaded = MutableLiveData<Boolean>()
    val isAllUILoaded: LiveData<Boolean> get() = _isAllUILoaded

    private val _isNewMessageUIClicked = MutableLiveData<Boolean>()
    val isNewMessageUIClicked: LiveData<Boolean> get() = _isNewMessageUIClicked

    private val _latestMessages = MutableLiveData<ArrayList<Message>>()
    val latestMessages: LiveData<ArrayList<Message>> get() = _latestMessages

    private val _usersOfLatestMessages = MutableLiveData<ArrayList<User>>()
    val usersOfLatestMessages: LiveData<ArrayList<User>> get() = _usersOfLatestMessages

    private val userManager: UserManager by inject()
    private val messageManager: MessageManager by inject { parametersOf("") }


    init {
        _isAllUILoaded.value = false
        _isNewMessageUIClicked.value = false

        getLatestMessages()
        viewModelScope.launch {
            messageManager.latestMessages.collectLatest {
                _latestMessages.postValue(it)
                getUsersOfLatestMessages()
            }
        }

        getAllUsersFromDatabase()
        viewModelScope.launch {
            userManager.allUsers.collectLatest {
                _allUsers.postValue(it)
            }
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

                val uid = if (it.fromUid == fromId) {
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
                if (tempMessage.get(j).timeStamp!! > tempMessage.get(i).timeStamp!!) {
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
        _isAllUILoaded.value = true
    }

    fun setIsUILoaded(boolean: Boolean) {
        _isAllUILoaded.value = boolean
    }

    fun setNewMessageUIClicked(boolean: Boolean) {
        _isNewMessageUIClicked.value = boolean
    }


}


