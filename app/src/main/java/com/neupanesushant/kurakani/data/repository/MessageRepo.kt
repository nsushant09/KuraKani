package com.neupanesushant.kurakani.data.repository

import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType

interface MessageRepo {
    suspend fun sendMessage(message: String, messageType: MessageType)
    fun sendMessageUpdates(message: Message)
    suspend fun deleteMessage(timeStamp: String)

}