package com.neupanesushant.kurakani.domain.usecase.message_manager

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.gson.Gson
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.domain.WorkerCodes
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.domain.usecase.AuthenticatedUser
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.AudioDeliverPolicy
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.ImageDeliverPolicy
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.TextDeliverPolicy
import com.neupanesushant.kurakani.domain.usecase.messaging_notification.MessagingNotification
import com.neupanesushant.kurakani.domain.usecase.messaging_notification.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageSender(private val context: Context, private val friend: User) {

    private val workManager = WorkManager.getInstance(context)
    private val gson = Gson()

    private val notificationManager: NotificationManager =
        MessagingNotification(AuthenticatedUser.getInstance().getUser()!!, friend.fcmToken ?: "")

    private val fromId = FirebaseInstance.fromId

    companion object {
        private val MESSAGE_TYPE_POLICIES = hashMapOf(
            Pair(MessageType.IMAGE, OneTimeWorkRequestBuilder<ImageDeliverPolicy>()),
            Pair(MessageType.TEXT, OneTimeWorkRequestBuilder<TextDeliverPolicy>()),
            Pair(MessageType.AUDIO, OneTimeWorkRequestBuilder<AudioDeliverPolicy>()),
        )
    }

    fun send (message: String, messageType: MessageType) {
        val workRequest = getWorkRequest(message, messageType) ?: return
        beginUniqueWork(workRequest)
        observeWork(workRequest)
    }

    private fun getWorkRequest(message: String, messageType: MessageType): OneTimeWorkRequest? {
        val policy = MESSAGE_TYPE_POLICIES[messageType] ?: return null

        return policy.setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
            .setInputData(
                Data.Builder().putString(WorkerCodes.INPUT_MESSAGE, message)
                    .putString(WorkerCodes.INPUT_MESSAGE_TO_ID, friend.uid).build()
            )
            .build()
    }

    private fun beginUniqueWork(workRequest: OneTimeWorkRequest) {
        workManager.beginUniqueWork(
            WorkerCodes.SEND_MESSAGE_WORK,
            ExistingWorkPolicy.REPLACE,
            workRequest
        ).enqueue()
    }

    private fun observeWork(workRequest: OneTimeWorkRequest) {
        workManager.getWorkInfosForUniqueWorkLiveData(WorkerCodes.SEND_MESSAGE_WORK)
            .observeForever { workInfo ->
                val requestInfo = workInfo.find { it.id == workRequest.id }
                if (requestInfo?.state == WorkInfo.State.SUCCEEDED) {
                    val json = requestInfo.outputData.getString(WorkerCodes.RESULT_MESSAGE)
                    val message = gson.fromJson(json, Message::class.java)
                    sendMessageUpdates(message)
                }
            }
    }

    private fun sendMessageUpdates(message: Message) {
        notificationManager.send(message)
        val timeStamp = message.timeStamp
        val toId = friend.uid
        CoroutineScope(Dispatchers.IO).launch {
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

}