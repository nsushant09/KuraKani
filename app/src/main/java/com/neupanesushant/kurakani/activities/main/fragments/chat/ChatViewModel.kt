package com.neupanesushant.kurakani.activities.main.fragments.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.User
import kotlinx.coroutines.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ChatViewModel"
    private val firebaseAuth: FirebaseAuth
    private val firebaseDatabase: DatabaseReference

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> get() = _allUsers


    private val _isAllUILoaded = MutableLiveData<Boolean>()
    val isAllUILoaded: LiveData<Boolean> get() = _isAllUILoaded

    private val _isNewMessageUIClicked = MutableLiveData<Boolean>()
    val isNewMessageUIClicked: LiveData<Boolean> get() = _isNewMessageUIClicked

    private val _latestMessages = MutableLiveData<ArrayList<Message>>()
    val latestMessages: LiveData<ArrayList<Message>> get() = _latestMessages

    private val _usersOfLatestMessages = MutableLiveData<ArrayList<User>>()
    val usersOfLatestMessages: LiveData<ArrayList<User>> get() = _usersOfLatestMessages


    private val fromId: String

    init {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("/users/${firebaseAuth.uid}")
        _isAllUILoaded.value = false
        _isNewMessageUIClicked.value = false
        fromId = firebaseAuth.currentUser?.uid.toString()

        getLatestMessages()
        getAllUsersFromDatabase()
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
                _latestMessages.value = temp
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
        _isAllUILoaded.value = true
    }

    private fun getUserOfLatestMessagesSuspended(uid: String) {
        FirebaseDatabase.getInstance().getReference().child("users")
            .child(uid).get().addOnSuccessListener {
                val user = it.getValue(User::class.java)
                if (user != null) {
                    tempLatestMessageUsers.add(user)
                    _usersOfLatestMessages.value = tempLatestMessageUsers
                    sortLatestMessages()
                }

            }
    }

    fun sortLatestMessages() {
        val tempUser: ArrayList<User> = _usersOfLatestMessages.value!!
        val tempMessage: ArrayList<Message> = _latestMessages.value!!
        for (i in 0 until tempUser.size - 1) {
            for (j in i until tempUser.size) {
                if (tempMessage.get(j).timeStamp!! > tempMessage.get(i).timeStamp!!) {
                    val mTemp = tempMessage[i]
                    tempMessage[i] = tempMessage[j]
                    tempMessage[j] = mTemp

                    val uTemp = tempUser[i]
                    tempUser[i] = tempUser[j]
                    tempUser[j] = uTemp
                }
            }
        }

        _usersOfLatestMessages.value = tempUser
        _latestMessages.value = tempMessage

    }

    fun setIsUILoaded(boolean: Boolean) {
        _isAllUILoaded.value = boolean
    }

    fun setNewMessageUIClicked(boolean: Boolean) {
        _isNewMessageUIClicked.value = boolean
    }


}


