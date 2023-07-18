package com.neupanesushant.kurakani.data.imagepersistence

import android.net.Uri
import com.neupanesushant.kurakani.data.FirebaseInstance
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DatabaseImagePersistence() : ImagePersistence {

    override suspend fun saveImage(imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val timeStamp = System.currentTimeMillis() / 100
            val ref = FirebaseInstance.firebaseStorage.getReference("/images/$timeStamp")
            val deferred = CompletableDeferred<String>()
            ref.putFile(imageUri).addOnSuccessListener {
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