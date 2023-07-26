package com.neupanesushant.kurakani.domain.usecase

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadFileUseCase(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO);

    fun download(message: Message) {
        message.messageType?.let { messageType ->
            when (messageType) {
                MessageType.IMAGE -> downloadData(
                    message.messageBody ?: "",
                    ".jpg",
                    "Image Download",
                    "Downloading Image"
                )
                MessageType.AUDIO -> downloadData(
                    message.messageBody ?: "",
                    ".mp3",
                    "Audio Download",
                    "Downloading Audio Message"
                )
                else -> null
            }
        }
    }

    private fun downloadData(
        url: String,
        fileExtension: String,
        notificationTitle: String,
        notificationDescription: String
    ) {
        coroutineScope.launch {
            downloadDataSuspend(url, fileExtension, notificationTitle, notificationDescription)
        }
    }

    private suspend fun downloadDataSuspend(
        url: String,
        fileExtension: String,
        notificationTitle: String,
        notificationDescription: String
    ) {
        withContext(Dispatchers.IO) {
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(url)
            val timestamp = System.currentTimeMillis()
            val request = DownloadManager.Request(uri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            request.setAllowedOverRoaming(false)
            request.setTitle(notificationTitle)
            request.setDescription(notificationDescription)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$timestamp$fileExtension"
            )

            downloadManager.enqueue(request)
        }
    }
}