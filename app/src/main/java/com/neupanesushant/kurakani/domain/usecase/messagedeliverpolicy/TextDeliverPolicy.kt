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
    fun sendMessage(message: String, toId: String): Message {
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
        val toId = inputData.getString(WorkerCodes.INPUT_MESSAGE_TO_ID) ?: return Result.failure()
        val result = sendMessage(message, toId)
        val resultJson = Gson().toJson(result)
        return Result.success(
            workDataOf(WorkerCodes.RESULT_MESSAGE to resultJson)
        )
    }
}