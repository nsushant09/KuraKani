package com.neupanesushant.kurakani.ui.main.fragments.chat

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.data.MessageManager
import com.neupanesushant.kurakani.data.UserManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class ChatViewModel(private val application: Application) : ViewModel(),
    KoinComponent {


    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val userManager: UserManager by inject()
    private val messageManager: MessageManager by inject { parametersOf("") }

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> get() = _allUsers

    private val _latestMessages = MutableLiveData<ArrayList<Message>>()
    val latestMessages: LiveData<ArrayList<Message>> get() = _latestMessages

    private val _usersOfLatestMessages = MutableLiveData<ArrayList<User>>()
    val usersOfLatestMessages: LiveData<ArrayList<User>> get() = _usersOfLatestMessages


    init {
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
                getUsersOfLatestMessages()
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
        uiScope.launch {

            if (_latestMessages.value == null)
                return@launch

            if (_latestMessages.value!!.isEmpty()) {
                sortLatestMessages(arrayListOf())
                return@launch
            }

            if (_allUsers.value.isNullOrEmpty())
                return@launch

            val deferredList = _latestMessages.value!!.map { message ->
                val uid = if (message.fromUid == FirebaseInstance.fromId) {
                    message.toUid ?: ""
                } else {
                    message.fromUid ?: ""
                }


                async {
                    _allUsers.value?.filter {
                        it.uid == uid
                    }?.get(0)
                }
            }

            val userList = deferredList.awaitAll().filterNotNull().toCollection(arrayListOf())
            sortLatestMessages(userList)
        }
    }

    private fun sortLatestMessages(usersOfLatestMessages: ArrayList<User>) {


        val tempUser: ArrayList<User> = usersOfLatestMessages
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
    }

}


