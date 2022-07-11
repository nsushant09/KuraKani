package com.neupanesushant.kurakani.activities.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.neupanesushant.kurakani.classes.Friend
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.User
import kotlinx.coroutines.*

class MainViewModel() : ViewModel() {

    private val TAG = "MainViewModel"
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseDatabase: DatabaseReference

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _user = MutableLiveData<User?>()
    val user get() = _user

    private val _friendUser = MutableLiveData<User?>()
    val friendUser get() = _friendUser

    private val _isFriendValueLoaded = MutableLiveData<Boolean>()
    private val isFriendValueLoaded get() = _isFriendValueLoaded

    init{
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("/users/${firebaseAuth.uid}")
        getUserFromDatabase()
    }

    fun getUserFromDatabase() {
        uiScope.launch {
            getUserFromDatabaseSuspended()
        }
    }

    private suspend fun getUserFromDatabaseSuspended() {
        withContext(Dispatchers.IO) {
            FirebaseDatabase.getInstance().getReference().child("users")
                .child(firebaseAuth.uid.toString()).get().addOnSuccessListener {
                    _user.value = it.getValue(User::class.java)
                }.addOnFailureListener {
                }
        }
    }

    fun getFriendUserFromDatabase(uid : String){
        uiScope.launch{
            getFriendUserFromDatabaseSuspended(uid)
        }
    }

    private suspend fun getFriendUserFromDatabaseSuspended(uid : String) {
        withContext(Dispatchers.IO){
            FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid).get().addOnSuccessListener {
                    _friendUser.value = it.getValue(User::class.java)
                }.addOnFailureListener{
                }
        }
    }

    fun nullFriendUser(){
        _friendUser.value = null
    }

    fun updateUser(user : User) {
        firebaseDatabase.setValue(user)
    }
}