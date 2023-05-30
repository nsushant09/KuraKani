package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType

interface MessageRepo {

    val firebaseAuth: FirebaseAuth get() = FirebaseAuth.getInstance()

    val firebaseStorage: FirebaseStorage get() = FirebaseStorage.getInstance()

    val firebaseDatabase : FirebaseDatabase get() = FirebaseDatabase.getInstance()

    val firebaseUser get() = firebaseAuth.currentUser!!

    val fromId get() = firebaseUser.uid

    suspend fun sendTextMessage(chatMessage : String)
    suspend fun sendImageMessage(imageWithKey : Pair<String, Uri>, callback: MessageManager.MessageCallback)
    suspend fun sendMessage(message : Message, timeStamp: Long)
    suspend fun deleteMessage(timeStamp : String)

    suspend fun getAllMessage()
    suspend fun getMessageUpdate()
}