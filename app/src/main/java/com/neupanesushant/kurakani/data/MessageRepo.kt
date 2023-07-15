package com.neupanesushant.kurakani.data

import android.net.Uri
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType
import com.neupanesushant.kurakani.data.messagedeliverpolicy.MessageDeliverPolicy
import kotlinx.coroutines.flow.Flow

interface MessageRepo {
    suspend fun sendMessage(message : String, messageType : MessageType)
    suspend fun sendMessageUpdates(message : Message, timeStamp: Long)
    suspend fun deleteMessage(timeStamp : String)
    suspend fun getLatestMessage()

}