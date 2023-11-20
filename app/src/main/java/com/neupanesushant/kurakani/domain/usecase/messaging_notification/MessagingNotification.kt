package com.neupanesushant.kurakani.domain.usecase.messaging_notification

import com.neupanesushant.kurakani.BuildConfig
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import com.neupanesushant.kurakani.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MessagingNotification(
    private val currentUser: User,
    private val otherUserFCMToken: String
) : NotificationManager {

    override fun send(message: Message) {
        val jsonObject = JSONObject()

        val notificationObject = JSONObject()
        notificationObject.put("title", currentUser.firstName + " " + currentUser.lastName)
        notificationObject.put("body", getMessageBody(message))

        val dataObj = JSONObject()
        dataObj.put("userID", currentUser.uid)

        jsonObject.put("notification", notificationObject)
        jsonObject.put("data", dataObj)
        jsonObject.put("to", otherUserFCMToken)

        callMessagingAPI(jsonObject)
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

    private fun callMessagingAPI(jsonObject: JSONObject) {
        CoroutineScope(Dispatchers.IO).launch {
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
}