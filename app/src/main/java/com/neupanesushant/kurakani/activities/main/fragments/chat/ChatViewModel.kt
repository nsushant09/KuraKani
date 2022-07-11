package com.neupanesushant.kurakani.activities.main.fragments.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.neupanesushant.kurakani.classes.Message
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
    val user: LiveData<User?> get() = _user

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> get() = _allUsers


    private val _isAllUILoaded = MutableLiveData<Boolean>()
    val isAllUILoaded: LiveData<Boolean> get() = _isAllUILoaded

    private val _isNewMessageUIClicked = MutableLiveData<Boolean>()
    val isNewMessageUIClicked: LiveData<Boolean> get() = _isNewMessageUIClicked

    private val _latestMessages = MutableLiveData<List<Message>>()
    val latestMessages: LiveData<List<Message>> get() = _latestMessages

    private val _usersOfLatestMessages = MutableLiveData<List<User>>()
    val usersOfLatestMessages: LiveData<List<User>> get() = _usersOfLatestMessages



    private val fromId: String

    init {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("/users/${firebaseAuth.uid}")
        firebaseUser = firebaseAuth.currentUser!!
        _isAllUILoaded.value = false
        _isNewMessageUIClicked.value = false

        fromId = firebaseAuth.currentUser?.uid.toString()

        getUserFromDatabase()
        getLatestMessages()
        getAllUsersFromDatabase()
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

    fun getAllUsersFromDatabase() {
        uiScope.launch {
            getAllUsersSuspended()
        }
    }

    private suspend fun getAllUsersSuspended() {
        withContext(Dispatchers.IO) {
            val ref = FirebaseDatabase.getInstance().getReference().child("users")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempList = ArrayList<User>()
                    snapshot.children.forEach {
                        if (it != null) {
                            val user: User = it.getValue(User::class.java)!!
                            tempList.add(user)
                        }
                    }
                    _allUsers.value = tempList.toList()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, "Cancelled")
                }

            })
        }
    }

    fun getLatestMessages() {
        val ref = FirebaseDatabase.getInstance().getReference().child("latest-messages")
            .child("${firebaseAuth.currentUser?.uid}")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = ArrayList<Message>()
                snapshot.children.forEach {
                    val message = it.getValue(Message::class.java)
                    if (message != null) {
                        temp.add(message)
                    }
                }
                _latestMessages.value = temp.toList()
                getUsersofLatestMessages()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    var tempLatestMessageUsers = ArrayList<User>()
    fun getUsersofLatestMessages() {
        tempLatestMessageUsers.clear()
        _latestMessages.value?.forEach {
            if (it.fromUid == firebaseAuth.currentUser?.uid) {
                getUserOfLatestMessagesSuspended(it.toUid!!)
            } else {
                getUserOfLatestMessagesSuspended(it.fromUid!!)
            }
        }

    }

    private fun getUserOfLatestMessagesSuspended(uid: String) {
        FirebaseDatabase.getInstance().getReference().child("users")
            .child(uid).get().addOnSuccessListener {
                val user = it.getValue(User::class.java)
                if (user != null) {
                    tempLatestMessageUsers.add(user)
                    _usersOfLatestMessages.value = tempLatestMessageUsers
                }

            }
    }


    fun setIsUILoaded(boolean: Boolean) {
        _isAllUILoaded.value = boolean
    }

    fun setNewMessageUIClicked(boolean: Boolean) {
        _isNewMessageUIClicked.value = boolean
    }


}


