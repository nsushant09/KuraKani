package com.neupanesushant.kurakani.services

import android.content.Context
import android.graphics.BitmapFactory
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.net.HttpURLConnection
import java.net.URL

class DownloadService(private val context : Context) : KoinComponent{

    private val imageNotificationService : NotificationService by inject{ parametersOf(
        NotificationService.NotificationType.IMAGE
    ) };
    private val coroutineScope = CoroutineScope(Dispatchers.IO);

    fun downloadImage(imageUrl: String) {
        val url = URL(imageUrl)
        val connection = url.openConnection() as HttpURLConnection
        coroutineScope.launch {
            downloadImage(connection)
        }
    }

    private suspend fun downloadImage(connection: HttpURLConnection) {
        withContext(Dispatchers.IO) {

            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            kotlin.runCatching {
                inputStream.close()
            }
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "Image",
                "Image downloaded from the internet"
            )

            imageNotificationService.sendNotification();
        }
    }
}