package com.neupanesushant.kurakani.data

import android.net.Uri
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.data.imagepersistence.DatabaseImagePersistence
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterAndLogin {

    interface Callback {
        fun onSuccess()
        fun onFailure(failureReason: String)
    }

    suspend fun login(email: String, password: String, callback: Callback) {
        withContext(Dispatchers.IO) {
            FirebaseInstance.firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        callback.onSuccess()
                    } else {
                        callback.onFailure(it.exception.toString())
                    }
                }
        }
    }


    suspend fun createNewUser(email: String, password: String, callback: Callback) {
        withContext(Dispatchers.IO) {
            FirebaseInstance.firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { callback.onSuccess() }
                .addOnFailureListener { callback.onFailure(it.toString()) }
        }
    }

    suspend fun addUser(
        firstname: String,
        lastname: String,
        imageUri: Uri?,
        callback: Callback,
    ) {
        withContext(Dispatchers.IO) {
            if (imageUri != null) {
                val imageUrl = DatabaseImagePersistence.getInstance().saveImage(imageUri)
                val user = User(
                    FirebaseInstance.firebaseAuth.uid,
                    firstname,
                    lastname,
                    "$firstname $lastname",
                    imageUrl
                )

                FirebaseInstance.firebaseDatabase.getReference("/users/${FirebaseInstance.firebaseAuth.uid}")
                    .setValue(user)
                    .addOnSuccessListener {
                        callback.onSuccess()
                    }.addOnFailureListener {
                        callback.onFailure(it.toString())
                    }
            }
        }
    }
}