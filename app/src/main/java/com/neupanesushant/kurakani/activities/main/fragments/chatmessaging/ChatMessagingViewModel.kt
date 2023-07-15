package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.app.Application
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType
import com.neupanesushant.kurakani.classes.User
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

class ChatMessagingViewModel(
    private val application: Application,
    private val friendUID: String
) :
    ViewModel(), KoinComponent {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _chatLog = MutableLiveData<List<Message>>()
    val chatLog get() = _chatLog

    private val _sentImagesList = MutableLiveData<java.util.ArrayList<Uri>>()
    private val sentImagesList get() = _sentImagesList
    private var sentImageUploadCounter = 0

    private val _user = MutableLiveData<User?>()
    val user get() = _user

    private val _friendUser = MutableLiveData<User?>()
    val friendUser get() = _friendUser

    private val _isFriendValueLoaded = MutableLiveData<Boolean>()

    private val userManager: UserManager by inject()
    private val messageManager: MessageManager by inject { parametersOf(friendUID) }


    init {
        viewModelScope.launch {
            messageManager.messages.collectLatest {
                _chatLog.postValue(it)
            }
        }
    }


    fun setUser(user: User) {
        _user.postValue(user)
    }

    fun deleteMessage(timeStamp: String) {
        uiScope.launch {
            messageManager.deleteMessage(timeStamp)
        }
    }

    fun sendTextMessage(chatMessage: String) {
        uiScope.launch {
            messageManager.sendMessage(chatMessage, MessageType.TEXT)
        }
    }

    fun sendImagesMessage(imageList: ArrayList<Uri>) {
        sentImageUploadCounter = 0
        _sentImagesList.value = imageList
        if (sentImagesList.value != null) {
            sendSingleImageMessage(sentImageUploadCounter)
        }

    }

    private fun sendSingleImageMessage(index: Int) {
        uiScope.launch {
            if (index < sentImagesList.value!!.size) {
                messageManager.sendMessage(
                    sentImagesList.value!![index].toString(),
                    MessageType.IMAGE
                )
                sendSingleImageMessage(index + 1)
            }
        }
    }

    fun getFriendUserDetails(uid: String) {
        _isFriendValueLoaded.value = false
        uiScope.launch {
            userManager.getSelectedUser(uid) { user ->
                _friendUser.postValue(user)
                _isFriendValueLoaded.postValue(true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
