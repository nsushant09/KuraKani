package com.neupanesushant.kurakani.domain.usecase.message_manager

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.neupanesushant.kurakani.domain.WorkerCodes
import com.neupanesushant.kurakani.domain.model.MessageType
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.AudioDeliverPolicy
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.ImageDeliverPolicy
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.MessageUpdateWorker
import com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy.TextDeliverPolicy

class MessageSender(private val context: Context, private val friend: User) {

    private val workManager = WorkManager.getInstance(context)

    companion object {
        private val MESSAGE_TYPE_POLICIES = hashMapOf(
            Pair(MessageType.IMAGE, OneTimeWorkRequestBuilder<ImageDeliverPolicy>()),
            Pair(MessageType.TEXT, OneTimeWorkRequestBuilder<TextDeliverPolicy>()),
            Pair(MessageType.AUDIO, OneTimeWorkRequestBuilder<AudioDeliverPolicy>()),
        )
    }

    fun send(message: String, messageType: MessageType) {
        val workRequest = getWorkRequest(message, messageType) ?: return
        beginUniqueWork(workRequest)
    }

    private fun getWorkRequest(message: String, messageType: MessageType): OneTimeWorkRequest? {
        val policy = MESSAGE_TYPE_POLICIES[messageType] ?: return null

        return policy.setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        ).setInputData(
            Data.Builder().putString(WorkerCodes.INPUT_MESSAGE, message)
                .putString(WorkerCodes.FRIEND_UID, friend.uid)
                .putString(WorkerCodes.FRIEND_FCM_TOKEN, friend.fcmToken).build()

        ).build()
    }

    private fun beginUniqueWork(workRequest: OneTimeWorkRequest) {
        val updateWorker = OneTimeWorkRequestBuilder<MessageUpdateWorker>().setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        ).build()

        workManager.beginUniqueWork(
            "SEND_MESSAGE",
            ExistingWorkPolicy.APPEND,
            workRequest
        )
            .then(updateWorker)
            .enqueue()
    }


}