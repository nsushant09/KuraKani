package com.neupanesushant.kurakani.domain.usecase

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

class CameraUseCase(private val context: Context) {

    private var lastCapturedFileName = "";
    fun getCaptureImageIntent(): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val currentTime = System.currentTimeMillis().toString()
        val file = File(context.cacheDir, "$currentTime.jpeg")
        val uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )
        lastCapturedFileName = "$currentTime.jpeg"
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        return intent
    }

    fun getLastCapturedFileName(): String = lastCapturedFileName;
    fun removeLastCapturedFile() {
        val file = File(context.cacheDir, getLastCapturedFileName())
        if (file.exists())
            file.delete()
    }

}