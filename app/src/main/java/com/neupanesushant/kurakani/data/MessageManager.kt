package com.neupanesushant.kurakani.data

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.AudioDeliverPolicy
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.ImageDeliverPolicy
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.MessageDeliverPolicy
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.TextDeliverPolicy
import com.neupanesushant.kurakani.data.repository.MessageRepo
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class MessageManager(val toId: String) : MessageRepo, KoinComponent {

    private val fromId = FirebaseInstance.fromId
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    val messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())
    val latestMessages: MutableStateFlow<ArrayList<Message>> = MutableStateFlow(arrayListOf())


    override suspend fun sendMessage(message: String, messageType: MessageType) {

        val messageTypePolicies = hashMapOf<MessageType, MessageDeliverPolicy>(
            Pair(MessageType.IMAGE, ImageDeliverPolicy(this)),
            Pair(MessageType.TEXT, TextDeliverPolicy(this)),
            Pair(MessageType.AUDIO, AudioDeliverPolicy(this)),
        )
        val policy = messageTypePolicies[messageType]
        policy?.sendMessage(message)
    }

    override suspend fun sendMessageUpdates(message: Message, timeStamp: Long) {

        scope.launch {
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

            FirebaseInstance.firebaseDatabase.reference.updateChildren(updates)
        }
    }

    override suspend fun deleteMessage(timeStamp: String) {
        scope.launch {
            FirebaseInstance.firebaseDatabase.getReference("/user-messages/$fromId$toId/$fromId$timeStamp$toId")
                .removeValue()
        }
    }

    override suspend fun getLatestMessage() {
        scope.launch {
            FirebaseInstance.firebaseDatabase.reference.child("latest-messages")
                .child(fromId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tempList = ArrayList<Message>()
                        snapshot.children.forEach {
                            it.getValue(Message::class.java)?.let { message ->
                                tempList.add(message)
                            }
                        }
                        latestMessages.value = tempList
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }
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
        }
    }

    init {
        scope.launch {
            FirebaseInstance.firebaseDatabase.getReference("/user-messages/$fromId$toId")
                .addChildEventListener(chatChildEventListener)
        }
    }

}