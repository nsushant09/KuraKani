package com.neupanesushant.kurakani.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

object FirebaseInstance {
    val firebaseAuth: FirebaseAuth get() = FirebaseAuth.getInstance()

    val firebaseStorage: FirebaseStorage get() = FirebaseStorage.getInstance()

    val firebaseDatabase : FirebaseDatabase get()  = FirebaseDatabase.getInstance("https://kurakani-asia-default-rtdb.asia-southeast1.firebasedatabase.app")

    val firebaseCloudMessage get() = FirebaseMessaging.getInstance()

    private val firebaseUser get() = firebaseAuth.currentUser!!

    val fromId get() = firebaseUser.uid

}