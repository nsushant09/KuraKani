package com.neupanesushant.kurakani.data

import android.net.Uri
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.auth.AuthResult
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.domain.usecase.databasepersistence.DatabaseImagePersistence
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class RegisterAndLogin {

    suspend fun login(email: String, password: String): AuthResult = coroutineScope {
        val job = async {
            FirebaseInstance.firebaseAuth.signInWithEmailAndPassword(email, password)
        }
        await(job.await())
    }


    suspend fun createNewUser(email: String, password: String): AuthResult = coroutineScope {
        val job =
            async { FirebaseInstance.firebaseAuth.createUserWithEmailAndPassword(email, password) }
        await(job.await())
    }

    suspend fun addUser(
        firstname: String,
        lastname: String,
        imageUri: Uri?,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            if (imageUri == null) {return@withContext false}

            val imageUrl = DatabaseImagePersistence().save(imageUri)
            val user = User(
                FirebaseInstance.firebaseAuth.uid,
                firstname,
                lastname,
                "$firstname $lastname",
                imageUrl
            )

            val deferred = CompletableDeferred<Boolean>()
            FirebaseInstance.firebaseDatabase.getReference("/users/${FirebaseInstance.firebaseAuth.uid}")
                .setValue(user)
                .addOnSuccessListener {
                    deferred.complete(true)
                }

            deferred.await()
        }
    }
}