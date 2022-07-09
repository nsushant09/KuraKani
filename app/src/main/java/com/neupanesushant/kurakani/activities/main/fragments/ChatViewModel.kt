package com.neupanesushant.kurakani.activities.main.fragments

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.neupanesushant.kurakani.classes.User
import kotlinx.coroutines.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ChatViewModel"
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseDatabase: DatabaseReference

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _user = MutableLiveData<User?>()
    val user get() = _user

    private val _allUsers = MutableLiveData<List<User?>>()
    private val allUsers get() = _allUsers

    init {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("/users/${firebaseAuth.uid}")
        firebaseUser = firebaseAuth.currentUser!!

        getUserFromDatabase()
        getAllUsersFromDatabase()
    }

    private fun getUserFromDatabase() {
        uiScope.launch {
            getUserFromDatabaseSuspended()
        }
    }

    suspend fun getUserFromDatabaseSuspended() {
        withContext(Dispatchers.IO) {
            FirebaseDatabase.getInstance().getReference().child("users")
                .child(firebaseAuth.uid.toString()).get().addOnSuccessListener {
                _user.value = it.getValue(User::class.java)
            }.addOnFailureListener {
                Log.i(TAG, "Failure to get user data")
            }
        }
    }

    private fun getAllUsersFromDatabase() {
        uiScope.launch {
            getAllUsersSuspended()
        }
    }

    suspend fun getAllUsersSuspended() {
        withContext(Dispatchers.IO) {
            val ref = FirebaseDatabase.getInstance().getReference().child("users")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempList = ArrayList<User?>()
                    snapshot.children.forEach {
                        val user = it.getValue(User::class.java)
                        tempList.add(user)
                        Log.i(TAG, "The name of the user is : " + user?.fullName)
                    }
                    _allUsers.value = tempList.toList()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, "Cancelled")
                }

            })
        }
    }


}


