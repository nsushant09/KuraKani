package com.neupanesushant.kurakani.data.messagedeliverpolicy

import android.net.Uri
import com.neupanesushant.kurakani.data.FirebaseInstance
import com.neupanesushant.kurakani.data.MessageManager
import com.neupanesushant.kurakani.data.databasepersistence.DatabaseImagePersistence
import com.neupanesushant.kurakani.model.Message
import com.neupanesushant.kurakani.model.MessageType

class ImageDeliverPolicy(private val messageManager: MessageManager) : MessageDeliverPolicy {
    override suspend fun sendMessage(message: String) {
        val timeStamp = System.currentTimeMillis() / 100;
        val imageUrl = DatabaseImagePersistence().save(Uri.parse(message))
        val messageObj = Message(
            FirebaseInstance.firebaseAuth.uid,
            messageManager.toId,
            MessageType.IMAGE,
            imageUrl,
            timeStamp
        )
        messageManager.sendMessageUpdates(messageObj, timeStamp)
    }
}