package com.neupanesushant.kurakani.activities.services

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime

class ShareService(private val context: Context?) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO);

    fun shareImage(imageUrl: String) {
        val url = URL(imageUrl)
        val connection = url.openConnection() as HttpURLConnection
        coroutineScope.launch {
            shareImage(connection)
        }
    }

    private suspend fun shareImage(connection: HttpURLConnection) {
        withContext(Dispatchers.IO) {
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val tempFile = File(context!!.cacheDir, System.currentTimeMillis().toString() + ".jpeg")
            try {
                kotlin.runCatching {
                    inputStream.close()
                    val fos = FileOutputStream(tempFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.close()
                }

                val intent = Intent(Intent.ACTION_SEND)
                intent.setDataAndType(
                    FileProvider.getUriForFile(
                        context!!,
                        context!!.applicationContext.packageName + ".provider",
                        tempFile
                    ),
                    "image/jpeg"
                )
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val chooser = Intent.createChooser(intent, "Send Image Via...")
                context.startActivity(chooser)
            } catch (e: Exception) {
                Log.i("TAG", e.printStackTrace().toString())
            }
        }
    }
}