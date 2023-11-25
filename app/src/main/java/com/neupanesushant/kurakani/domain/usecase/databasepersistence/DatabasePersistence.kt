package com.neupanesushant.kurakani.domain.usecase.databasepersistence

import android.net.Uri

interface DatabasePersistence {
    suspend operator fun invoke(uri : Uri) : String
}