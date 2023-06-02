package com.neupanesushant.kurakani.data

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.neupanesushant.kurakani.classes.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterAndLogin : FirebaseInstance {

    interface Callback {
        fun onSuccess()
        fun onFailure(failureReason: String)
    }

    interface ImageCallback {
        fun onImageAdded(downloadUrl: String);
        fun onImageDeclined();
    }

    suspend fun login(email: String, password: String, callback: Callback) {
        withContext(Dispatchers.IO) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
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
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { callback.onSuccess() }
                .addOnFailureListener { callback.onFailure(it.toString()) }
        }
    }

    suspend fun addUser(firstname: String, lastname: String, image: Uri?, callback: Callback) {
        withContext(Dispatchers.IO) {
            if (image != null) {
                addImageToDatabase(image, object : ImageCallback {
                    override fun onImageAdded(downloadUrl: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            addUser(firstname, lastname, downloadUrl, callback)
                        }
                    }

                    override fun onImageDeclined() {
                    }

                })
            }
        }
    }

    private suspend fun addUser(
        firstname: String,
        lastname: String,
        imageUrl: String,
        callback: Callback,
    ) {
        withContext(Dispatchers.IO) {
            val user = User(
                firebaseAuth.uid,
                firstname,
                lastname,
                "$firstname $lastname",
                imageUrl
            )

            firebaseDatabase.getReference("/users/${firebaseAuth.uid}").setValue(user)
                .addOnSuccessListener {
                    callback.onSuccess()
                }.addOnFailureListener {
                    callback.onFailure(it.toString())
                }
        }
    }

    suspend fun addImageToDatabase(image: Uri, imageCallback: ImageCallback) {
        withContext(Dispatchers.IO) {
            val timeStamp = System.currentTimeMillis() / 100
            val ref = firebaseStorage.getReference("/images/$timeStamp")
            ref.putFile(image).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    imageCallback.onImageAdded(downloadUrl.toString())
                }
            }
        }
    }
}