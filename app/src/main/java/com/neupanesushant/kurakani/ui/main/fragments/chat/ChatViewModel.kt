package com.neupanesushant.kurakani.ui.main.fragments.chat

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neupanesushant.kurakani.data.MessageManager
import com.neupanesushant.kurakani.data.UserManager
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

    private val _latestMessages = MutableLiveData<List<Message>>()
    val latestMessages: LiveData<List<Message>> get() = _latestMessages

    private val _usersOfLatestMessages = MutableLiveData<ArrayList<User>>()
    val usersOfLatestMessages: LiveData<ArrayList<User>> get() = _usersOfLatestMessages


    init {

        viewModelScope.launch {
            async { getAllUsersFromDatabase() }
            async { getLatestMessages() }

        }

        viewModelScope.launch {
            messageManager.latestMessages.collectLatest {
                _latestMessages.postValue(it)
                sortLatestMessages()
            }
        }

        viewModelScope.launch {
            userManager.allUsers.collectLatest {
                _allUsers.postValue(it)
                sortLatestMessages()
            }
        }
    }

    private suspend fun getAllUsersFromDatabase() {
        userManager.getAllUser()
    }

    private suspend fun getLatestMessages() {
        messageManager.getLatestMessage()
    }

    private suspend fun sortLatestMessages() {

        if (_latestMessages.value == null)
            return

        if (_allUsers.value == null)
            return


        // Get Users accordingly
        uiScope.launch {
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


            _usersOfLatestMessages.value =
                deferredList.awaitAll().filterNotNull().toCollection(arrayListOf())
        }

    }

}


