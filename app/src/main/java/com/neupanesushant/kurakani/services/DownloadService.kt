package com.neupanesushant.kurakani.services

import android.app.DownloadManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
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

class DownloadService(private val context: Context) : KoinComponent {

    private val coroutineScope = CoroutineScope(Dispatchers.IO);
    fun downloadImage(imageUrl: String) {
        coroutineScope.launch {
            downloadImageUsingManager(imageUrl)
        }
    }

    private suspend fun downloadImageUsingManager(imageUrl: String) {
        withContext(Dispatchers.IO) {
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(imageUrl)
            val timestamp = System.currentTimeMillis()
            val request = DownloadManager.Request(uri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            request.setAllowedOverRoaming(false)
            request.setTitle("Downloading Image")
            request.setDescription("Image Download")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$timestamp.jpg"
            )

            downloadManager.enqueue(request)
        }
    }
}