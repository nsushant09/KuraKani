package com.neupanesushant.kurakani.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.gson.Gson
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.data.repository.MessageRepo
import com.neupanesushant.kurakani.domain.WorkerCodes
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.AudioDeliverPolicy
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.ImageDeliverPolicy
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.TextDeliverPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class MessageManager(val context: Context, val toId: String) : MessageRepo, KoinComponent {

    private val fromId = FirebaseInstance.fromId
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val workManager = WorkManager.getInstance(context)
    private val gson = Gson()

    val messages: MutableStateFlow<MutableList<Message>> = MutableStateFlow(mutableListOf())
    val latestMessages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())


    override suspend fun sendMessage(message: String, messageType: MessageType) {

        val messageTypePolicies = hashMapOf<MessageType, OneTimeWorkRequest.Builder>(
            Pair(MessageType.IMAGE, OneTimeWorkRequestBuilder<ImageDeliverPolicy>()),
            Pair(MessageType.TEXT, OneTimeWorkRequestBuilder<TextDeliverPolicy>()),
            Pair(MessageType.AUDIO, OneTimeWorkRequestBuilder<AudioDeliverPolicy>()),
        )
        val policy = messageTypePolicies[messageType] ?: return

        val messageWorkRequest = policy.setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
            .setInputData(
                Data.Builder().putString(WorkerCodes.INPUT_MESSAGE, message)
                    .putString(WorkerCodes.INPUT_MESSAGE_TO_ID, toId).build()
            )
            .build()

        workManager.beginUniqueWork(
            WorkerCodes.SEND_MESSAGE_WORK,
            ExistingWorkPolicy.APPEND,
            messageWorkRequest
        ).enqueue()

        workManager.getWorkInfosForUniqueWorkLiveData(WorkerCodes.SEND_MESSAGE_WORK)
            .observeForever { workInfo ->
                val requestInfo = workInfo.find { it.id == messageWorkRequest.id }
                when (requestInfo?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        val messageJson =
                            requestInfo.outputData.getString(WorkerCodes.RESULT_MESSAGE)
                        val messageFinal = gson.fromJson(messageJson, Message::class.java)
                        sendMessageUpdates(messageFinal)
                    }

                    else -> {}
                }
            }
    }

    override fun sendMessageUpdates(message: Message) {
        val timeStamp = message.timeStamp

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
                .addValueEventListener(valueEventListener)
        }
    }

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val tempList =
                snapshot.getValue<HashMap<String, Message>>()
                    ?.values
                    ?.sortedByDescending { it.timeStamp }
                    ?: return

            CoroutineScope(Dispatchers.IO).launch {
                latestMessages.emit(tempList)
            }
        }

        override fun onCancelled(error: DatabaseError) {
        }

    }

    private val chatChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val message = snapshot.getValue(Message::class.java) ?: return
            val currentMessages = messages.value.toMutableList()
            currentMessages.add(message)
            messages.value = currentMessages
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