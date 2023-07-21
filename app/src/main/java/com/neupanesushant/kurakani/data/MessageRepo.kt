package com.neupanesushant.kurakani.data

import com.neupanesushant.kurakani.model.Message
import com.neupanesushant.kurakani.model.MessageType

interface MessageRepo {
    suspend fun sendMessage(message : String, messageType : MessageType)
    suspend fun sendMessageUpdates(message : Message, timeStamp: Long)
    suspend fun deleteMessage(timeStamp : String)
    suspend fun getLatestMessage()

}