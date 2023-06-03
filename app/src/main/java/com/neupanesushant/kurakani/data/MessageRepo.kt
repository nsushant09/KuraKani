package com.neupanesushant.kurakani.data

import android.net.Uri
import com.neupanesushant.kurakani.classes.Message

interface MessageRepo {
    suspend fun sendTextMessage(chatMessage : String)
    suspend fun sendImageMessage(image: Uri, callback: MessageManager.MessageCallback)
    suspend fun sendMessage(message : Message, timeStamp: Long)
    suspend fun deleteMessage(timeStamp : String)

    suspend fun getAllMessage()
    suspend fun getMessageUpdate()

    suspend fun getLatestMessage()

}