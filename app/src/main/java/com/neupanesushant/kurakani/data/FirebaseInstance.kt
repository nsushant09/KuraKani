package com.neupanesushant.kurakani.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

interface FirebaseInstance {
    val firebaseAuth: FirebaseAuth get() = FirebaseAuth.getInstance()

    val firebaseStorage: FirebaseStorage get() = FirebaseStorage.getInstance()

    val firebaseDatabase : FirebaseDatabase get() = FirebaseDatabase.getInstance()

    val firebaseUser get() = firebaseAuth.currentUser!!

    val fromId get() = firebaseUser.uid

}