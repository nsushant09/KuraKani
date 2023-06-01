package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.app.Application
import android.net.Uri

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neupanesushant.kurakani.services.CameraService
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.data.MessageManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.java.KoinJavaComponent.get

class ChatMessagingViewModel(
    private val application: Application,
    private val messageManager: MessageManager
) :
    ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val cameraService : CameraService = get(CameraService::class.java)

    private val _chatLog = MutableLiveData<List<Message>>()
    val chatLog get() = _chatLog

    private val _sentImagesList = MutableLiveData<kotlin.collections.LinkedHashMap<String, Uri?>>()
    private val sentImagesList get() = _sentImagesList
    private var sentImageUploadCounter = 0

    init {
        viewModelScope.launch {
            messageManager.messages.collectLatest {
                _chatLog.postValue(it)
            }
        }
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

    fun addImagesToDatabase(imageList: kotlin.collections.LinkedHashMap<String, Uri?>) {
        sentImageUploadCounter = 0
        _sentImagesList.value = imageList
        if (sentImagesList.value != null) {
            addSingleImageToDatabase(sentImageUploadCounter)
        }

    }

    private fun addSingleImageToDatabase(index: Int) {
        uiScope.launch {
            val keys = sentImagesList.value!!.keys
            if (index < keys.size) {
                val currentKey = keys.elementAt(index)
                messageManager.sendImageMessage(
                    Pair(
                        currentKey,
                        sentImagesList.value!![currentKey]!!
                    ),
                    object : MessageManager.MessageCallback {
                        override fun onImageSentSuccessfully() {
                            cameraService.removeLastCapturedFile()
                            addSingleImageToDatabase(index + 1)
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

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
