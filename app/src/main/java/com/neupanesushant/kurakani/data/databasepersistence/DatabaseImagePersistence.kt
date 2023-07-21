package com.neupanesushant.kurakani.data.databasepersistence

import android.net.Uri
import com.neupanesushant.kurakani.data.FirebaseInstance
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DatabaseImagePersistence : DatabasePersistence {

    override suspend fun save(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val timeStamp = System.currentTimeMillis() / 100
            val ref =
                FirebaseInstance.firebaseStorage.getReference("/images/${FirebaseInstance.fromId}$timeStamp")
            val deferred = CompletableDeferred<String>()
            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    deferred.complete(downloadUrl.toString())
                }.addOnFailureListener { exception ->
                    deferred.completeExceptionally(exception)
                }
            }.addOnFailureListener { exception ->
                deferred.completeExceptionally(exception)
            }
            deferred.await()
        }
    }
}