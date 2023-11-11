package com.neupanesushant.kurakani.domain.usecase.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager

import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.neupanesushant.kurakani.services.CAMERA_PERMISSION_CODE
import com.neupanesushant.kurakani.services.READ_EXTERNAL_STORAGE_PERMISSION_CODE
import com.neupanesushant.kurakani.services.RECORD_AUDIO_CODE


object PermissionManager {

    fun hasCameraPermission(context: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun requestCameraPermission(activity: Activity) {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        requestPermissions(
            activity,
            permissions,
            CAMERA_PERMISSION_CODE
        )
    }

    fun hasReadExternalStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

     fun requestReadExternalStoragePermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_EXTERNAL_STORAGE_PERMISSION_CODE
        )
    }

    fun hasRecordAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestRecordAudioPermission(activity: Activity) {
        requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_CODE
        )
    }

}