package com.neupanesushant.kurakani.data.databasepersistence

import android.net.Uri

interface DatabasePersistence {
    suspend fun save(uri: Uri): String
}