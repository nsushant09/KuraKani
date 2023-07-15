package com.neupanesushant.kurakani.data.messagedeliverpolicy

import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType
import com.neupanesushant.kurakani.data.FirebaseInstance
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

    companion object {
        private var instance: TextDeliverPolicy? = null
        fun getInstance(messageManager: MessageManager): TextDeliverPolicy {
            if (instance == null) {
                instance = TextDeliverPolicy(messageManager)
            }
            return instance!!
        }
    }
}