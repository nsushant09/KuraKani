package com.neupanesushant.kurakani.data.repository

import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType

interface MessageRepo {
    suspend fun sendMessage(message : String, messageType : MessageType)
    suspend fun sendMessageUpdates(message : Message, timeStamp: Long)
    suspend fun deleteMessage(timeStamp : String)
    suspend fun getLatestMessage()

}