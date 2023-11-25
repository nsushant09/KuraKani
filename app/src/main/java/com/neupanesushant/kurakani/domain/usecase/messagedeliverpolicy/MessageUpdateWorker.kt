package com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.domain.WorkerCodes
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.usecase.AuthenticatedUser
import com.neupanesushant.kurakani.domain.usecase.messaging_notification.MessagingNotification
import kotlinx.coroutines.coroutineScope

class MessageUpdateWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    private lateinit var notification: MessagingNotification
    override suspend fun doWork(): Result {
        setupNotification()
        val message = getMessageFromJson(inputData.getString(WorkerCodes.RESULT_MESSAGE) ?: "")
        performUpdates(message)
        return Result.success()
    }

    private fun setupNotification() {
        val fcmToken = inputData.getString(WorkerCodes.FRIEND_FCM_TOKEN)
        notification = MessagingNotification(fcmToken ?: "")
    }

    private fun getMessageFromJson(message: String): Message {
        return Gson().fromJson(message, Message::class.java)
    }

    private suspend fun performUpdates(message: Message) = coroutineScope {
        val toId = inputData.getString(WorkerCodes.FRIEND_UID)
        val fromId = AuthenticatedUser.getInstance().getUID()
        val timeStamp = message.timeStamp

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