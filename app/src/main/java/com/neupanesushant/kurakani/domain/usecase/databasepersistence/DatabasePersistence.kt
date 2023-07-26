package com.neupanesushant.kurakani.domain.usecase.databasepersistence

import android.net.Uri

interface DatabasePersistence {
    suspend fun save(uri: Uri): String
}