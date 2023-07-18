package com.neupanesushant.kurakani.data.messagedeliverpolicy

import android.net.Uri
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType
import com.neupanesushant.kurakani.data.FirebaseInstance
import com.neupanesushant.kurakani.data.MessageManager
import com.neupanesushant.kurakani.data.RegisterAndLogin
import com.neupanesushant.kurakani.data.imagepersistence.DatabaseImagePersistence
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ImageDeliverPolicy(private val messageManager: MessageManager) : MessageDeliverPolicy,
    KoinComponent {
    override suspend fun sendMessage(message: String) {
        val timeStamp = System.currentTimeMillis() / 100;
        val imageUrl = DatabaseImagePersistence().saveImage(Uri.parse(message))
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