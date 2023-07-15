package com.neupanesushant.kurakani.data.imagepersistence

import android.net.Uri

interface ImagePersistence {
    suspend fun saveImage(imageUri: Uri) : String
}