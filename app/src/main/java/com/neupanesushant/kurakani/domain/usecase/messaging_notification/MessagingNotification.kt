package com.neupanesushant.kurakani.domain.usecase.messaging_notification

import com.neupanesushant.kurakani.BuildConfig
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import com.neupanesushant.kurakani.domain.usecase.AuthenticatedUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MessagingNotification(
    private val otherUserFCMToken: String
) : NotificationManager {

    private val currentUser = AuthenticatedUser.getInstance().getUser()
    override fun send(message: Message) {
        if (currentUser == null || otherUserFCMToken.isEmpty()) return

        val jsonObject = JSONObject()

        val notificationObject = JSONObject()
        notificationObject.put("title", currentUser.firstName + " " + currentUser.lastName)
        notificationObject.put("body", getMessageBody(message))

        val dataObj = JSONObject()
        dataObj.put("userID", currentUser.uid)

        jsonObject.put("notification", notificationObject)
        jsonObject.put("data", dataObj)
        jsonObject.put("to", otherUserFCMToken)

        CoroutineScope(Dispatchers.IO).launch { callMessagingAPI(jsonObject) }
    }

    private fun getMessageBody(message: Message): String {
        return when (message.messageType) {
            MessageType.IMAGE -> {
                "Sent a photo"
            }

            MessageType.AUDIO -> {
                "Sent a voice message"
            }

            else -> {
                message.messageBody ?: ""
            }
        }
    }

    private suspend fun callMessagingAPI(jsonObject: JSONObject) = coroutineScope {
        val JSON = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val body = jsonObject.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header(
                "Authorization",
                "Bearer " + BuildConfig.FCM_TOKEN
            )
            .build()

        client.newCall(request).execute()

    }
}