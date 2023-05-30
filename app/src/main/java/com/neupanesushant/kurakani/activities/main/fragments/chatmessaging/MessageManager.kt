package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.net.Uri
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Dispatcher

class MessageManager(private val toId: String) : MessageRepo {


    val messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())

    interface MessageCallback {
        fun onImageSentSuccessfully();
    }

    override suspend fun sendTextMessage(chatMessage: String) {
        val timeStamp = System.currentTimeMillis() / 100
        val message = Message(
            firebaseAuth.uid,
            toId,
            MessageType.TEXT,
            chatMessage,
            timeStamp
        )
        sendMessage(message, timeStamp)
    }

    override suspend fun sendImageMessage(
        imageWithKey: Pair<String, Uri>,
        callback: MessageCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val timeStamp = System.currentTimeMillis() / 100
            val ref = firebaseStorage.getReference("/images/${imageWithKey.first}")

            ref.putFile(imageWithKey.second).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val message = Message(
                        firebaseAuth.uid,
                        toId,
                        MessageType.IMAGE,
                        downloadUrl.toString(),
                        timeStamp
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        sendMessage(message, timeStamp)
                    }.invokeOnCompletion {
                        callback.onImageSentSuccessfully()
                    }
                }
            }
        }
    }


    override suspend fun sendMessage(message: Message, timeStamp: Long) {

        withContext(Dispatchers.IO) {
            val fromMessagePath = "/user-messages/$fromId$toId/$fromId$timeStamp$toId"
            val toMessagePath = "/user-messages/$toId$fromId/$toId$timeStamp$fromId"
            val latestMessagePathFrom = "/latest-messages/$fromId/$toId"
            val latestMessagePathTo = "/latest-messages/$toId/$fromId"

            val updates = mapOf(
                fromMessagePath to message,
                toMessagePath to message,
                latestMessagePathFrom to message,
                latestMessagePathTo to message
            )

            firebaseDatabase.reference.updateChildren(updates)
        }
    }

    override suspend fun deleteMessage(timeStamp: String) {
        withContext(Dispatchers.IO) {
            firebaseDatabase.getReference("/user-messages/$fromId$toId/$fromId$timeStamp$toId")
                .removeValue()
        }
    }

    override suspend fun getMessageUpdate() {
        withContext(Dispatchers.IO) {
            firebaseDatabase.getReference("/user-messages/$fromId$toId")
                .addChildEventListener(chatChildEventListener)
        }
    }

    override suspend fun getAllMessage() {
        messages.value = emptyList()
        getMessageUpdate()
    }

    private val chatChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val currentMessages = messages.value.toMutableList()
            val message = snapshot.getValue(Message::class.java)
            message?.let {
                currentMessages.add(0, message)
                messages.value = currentMessages
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val currentMessages = messages.value.toMutableList()
            currentMessages.remove(snapshot.getValue(Message::class.java))
            messages.value = currentMessages
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onCancelled(error: DatabaseError) {
//            Toast.makeText(application.applicationContext, "Could not send message", Toast.LENGTH_SHORT).show()
        }
    }

}