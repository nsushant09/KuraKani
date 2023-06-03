package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.app.Application
import android.net.Uri
import android.widget.Toast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neupanesushant.kurakani.services.CameraService
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.data.MessageManager
import com.neupanesushant.kurakani.data.UserManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.get

class ChatMessagingViewModel(
    private val application: Application,
    private val friendUID: String
) :
    ViewModel(), KoinComponent {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val cameraService: CameraService = get(CameraService::class.java)

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
    val isFriendValueLoaded get() = _isFriendValueLoaded

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
            messageManager.sendTextMessage(chatMessage)
        }
    }

    fun addImagesToDatabase(imageList: ArrayList<Uri>) {
        sentImageUploadCounter = 0
        _sentImagesList.value = imageList
        if (sentImagesList.value != null) {
            addSingleImageToDatabase(sentImageUploadCounter)
        }

    }

    private fun addSingleImageToDatabase(index: Int) {
        uiScope.launch {
            if (index < sentImagesList.value!!.size) {
                messageManager.sendImageMessage(
                    sentImagesList.value!![index],
                    object : MessageManager.MessageCallback {

                        override fun onMessageSent() {
                            cameraService.removeLastCapturedFile()
                            addSingleImageToDatabase(index + 1)
                        }

                        override fun onMessageDeclined() {
                        }
                    }
                )
            }
        }
    }

    fun getAllMessages() {
        uiScope.launch {
            messageManager.getAllMessage()
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
