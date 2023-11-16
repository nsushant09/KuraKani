package com.neupanesushant.kurakani.domain.usecase.messaging_notification

import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import com.neupanesushant.kurakani.domain.model.User
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class MessagingNotification(
    private val currentUser: User,
    private val otherUserFCMToken: String
) {

    fun send(message: Message) {
        val jsonObject = JSONObject()

        val notificationObject = JSONObject()
        notificationObject.put("title", currentUser.firstName + " " + currentUser.lastName)
        notificationObject.put("body", getMessageBody(message))

        val dataObj = JSONObject()
        dataObj.put("friendUID", currentUser.uid)

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
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        val okHttpClient = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val body = RequestBody.create(JSON, jsonObject.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header(
                "Authorization",
                "Bearer AAAAQnPQ_Xw:APA91bGDz8xCJunNHcOKRps1gHozLHeGSphgGmajImi1TGB3pagtAV27i1MXUg4GOOnlHP7YJ6rz7kRBSW01fmXl3UJ-yXt1owVz7s71aFhvVcTYAQ4JVf69Q3B0wzBPtxLtCdhQvjbL"
            )
            .build()

        okHttpClient.newCall(request)
    }
}