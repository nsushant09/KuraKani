package com.neupanesushant.kurakani.activities.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager

import androidx.core.app.ActivityCompat


object PermissionManager {

    fun hasCameraPermission(context: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun requestCameraPermission(activity: Activity) {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(
            activity,
            permissions,
            CAMERA_PERMISSION_CODE
        )
    }

}