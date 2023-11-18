package com.neupanesushant.kurakani.ui.main.fragments.chatmessaging

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.domain.usecase.message_manager.ChatEventHandler
import com.neupanesushant.kurakani.domain.usecase.message_manager.MessageDeleter
import com.neupanesushant.kurakani.domain.usecase.message_manager.MessageSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class ChatMessagingViewModel(
    private val friend: User
) :
    ViewModel(), KoinComponent {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _chatLog = MutableLiveData<ArrayList<Message>>()
    val chatLog: LiveData<ArrayList<Message>> get() = _chatLog

    private val tempChatLog: ArrayList<Message> = arrayListOf()

    private val _sentImagesList = MutableLiveData<java.util.ArrayList<Uri>>()
    private val sentImagesList get() = _sentImagesList
    private var sentImageUploadCounter = 0

    private val messageSender: MessageSender by inject { parametersOf(friend) }
    private val messageDeleter: MessageDeleter by inject { parametersOf(friend) }
    private val chatEventHandler: ChatEventHandler by inject { parametersOf(friend) }


    init {
        viewModelScope.launch {
            chatEventHandler.messageWithAction.collectLatest {
                if (it == null) return@collectLatest
                if(it.second == ChatEventHandler.ACTION.ADD) tempChatLog.add(it.first)
                if(it.second == ChatEventHandler.ACTION.DELETE) tempChatLog.remove(it.first)
                _chatLog.value = tempChatLog
            }
        }
    }

    fun deleteMessage(timeStamp: String) {
        uiScope.launch {
            messageDeleter.delete(timeStamp)
        }
    }

    fun sendTextMessage(chatMessage: String) {
        uiScope.launch {
            messageSender.send(chatMessage, MessageType.TEXT)
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
                messageSender.send(
                    sentImagesList.value!![index].toString(),
                    MessageType.IMAGE
                )
                sendSingleImageMessage(index + 1)
            }
        }
    }

    fun sendAudioMessage(uri: Uri) {
        uiScope.launch {
            messageSender.send(
                uri.toString(),
                MessageType.AUDIO
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
