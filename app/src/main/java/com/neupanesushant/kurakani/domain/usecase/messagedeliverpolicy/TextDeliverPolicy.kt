package com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy

import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.data.MessageManager

class TextDeliverPolicy(private val messageManager: MessageManager) : MessageDeliverPolicy {
    override suspend fun sendMessage(message: String) {
        val timeStamp = System.currentTimeMillis() / 100
        val messageObj = Message(
            FirebaseInstance.firebaseAuth.uid,
            messageManager.toId,
            MessageType.TEXT,
            message,
            timeStamp
        )
        messageManager.sendMessageUpdates(messageObj, timeStamp)
    }
}