package com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.domain.WorkerCodes
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType

class TextDeliverPolicy(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    private suspend fun getMessageObject(message: String, toId: String): Message {
        val timeStamp = System.currentTimeMillis() / 100
        return Message(
            FirebaseInstance.firebaseAuth.uid,
            toId,
            MessageType.TEXT,
            message,
            timeStamp
        )
    }

    override suspend fun doWork(): Result {
        val message = inputData.getString(WorkerCodes.INPUT_MESSAGE) ?: return Result.failure()
        val toId = inputData.getString(WorkerCodes.FRIEND_UID) ?: return Result.failure()
        val result = getMessageObject(message, toId)
        val resultJson = Gson().toJson(result)
        return Result.success(
            workDataOf(
                WorkerCodes.RESULT_MESSAGE to resultJson,
                WorkerCodes.FRIEND_UID to inputData.getString(WorkerCodes.FRIEND_UID),
                WorkerCodes.FRIEND_FCM_TOKEN to inputData.getString(WorkerCodes.FRIEND_FCM_TOKEN)
            ),
        )
    }
}