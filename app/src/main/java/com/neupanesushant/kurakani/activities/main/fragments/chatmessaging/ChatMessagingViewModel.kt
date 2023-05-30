package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType
import kotlinx.coroutines.*

class ChatMessagingViewModel(application: Application) : AndroidViewModel(application) {

    private var firebaseUser: FirebaseUser
    private var firebaseAuth: FirebaseAuth
    private var firebaseStorage: FirebaseStorage

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val fromId: String
    private var toId = ""


    private val _chatLog = MutableLiveData<ArrayList<Message>>()
    val chatLog get() = _chatLog

    private val tempChatList = ArrayList<Message>()

    private val _sentImagesList = MutableLiveData<kotlin.collections.LinkedHashMap<String, Uri?>>()
    private val sentImagesList get() = _sentImagesList
    private var sentImageUploadCounter = 0

    init {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        fromId = firebaseAuth.currentUser!!.uid
        getChatUpdateFromDatabase()
    }

    fun setToID(uid: String) {
        toId = uid
    }

    fun deleteChatFromDatabase(timeStamp: String) {
        uiScope.launch {
            deleteChatFromDatabaseSuspended(timeStamp)
        }
    }

    private suspend fun deleteChatFromDatabaseSuspended(timeStamp: String) {
        withContext(Dispatchers.IO) {
            FirebaseDatabase.getInstance()
                .getReference("/user-messages/$fromId$toId/$fromId$timeStamp$toId").removeValue()
        }
    }

    fun addChatToDatabase(chatMessage: String, messageType: MessageType) {
        uiScope.launch {
            addChatToDatabaseSuspended(chatMessage, messageType)
        }
    }

    private suspend fun addChatToDatabaseSuspended(chatMessage: String, messageType: MessageType) {
        val timeStamp = System.currentTimeMillis() / 100
        val message = Message(firebaseAuth.uid, toId, messageType, chatMessage, timeStamp)

        withContext(Dispatchers.IO) {
            val fromMessagePath = "/user-messages/$fromId$toId/$fromId$timeStamp$toId"
            val toMessagePath = "/user-messages/$toId$fromId/$toId$timeStamp$fromId"
            val latestMessagePathFrom = "/latest-messages/$fromId/$toId"
            val latestMessagePathTo = "/latest-messages/$toId/$fromId"

            val updates = mapOf(
                fromMessagePath to message,
                toMessagePath to message,
                latestMessagePathFrom to message,
                latestMessagePathTo to message
            )

            FirebaseDatabase.getInstance().reference.updateChildren(updates)
        }
    }

    private fun getChatUpdateFromDatabase() {
        uiScope.launch {
            getChatUpdateSupended()
        }
    }

    private suspend fun getChatUpdateSupended() {
        withContext(Dispatchers.IO) {
            FirebaseDatabase.getInstance().getReference("/user-messages/$fromId$toId")
                .addChildEventListener(chatChildEventListener)
        }
    }

    fun addImagesToDatabase(imageList: kotlin.collections.LinkedHashMap<String, Uri?>) {
        sentImageUploadCounter = 0
        _sentImagesList.value = imageList
        if (sentImagesList.value != null) {
            addSingleImageToDatabase(sentImageUploadCounter)
        }

    }

    private fun addSingleImageToDatabase(index: Int) {
        val keys = sentImagesList.value!!.keys
        if (index < keys.size) {
            val currentKey = keys.elementAt(index)
            val ref = firebaseStorage.getReference("/images/${currentKey}")
            ref.putFile(sentImagesList.value!!.get(currentKey)!!).addOnSuccessListener {
                addSingleImageToDatabase(index + 1)
                ref.downloadUrl.addOnSuccessListener {
                    addChatToDatabase(it.toString(), MessageType.IMAGE)
                }
            }
        }

    }

    private val chatChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val message = snapshot.getValue(Message::class.java)
            message?.let {
                tempChatList.add(0, it)
                _chatLog.value = tempChatList
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            tempChatList.remove(snapshot.getValue(Message::class.java));
            _chatLog.value = tempChatList
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(application.applicationContext, "Could not send message", Toast.LENGTH_SHORT).show()
        }
    }

    fun getAllChatFromDatabase() {
        tempChatList.clear()
        getChatUpdateFromDatabase()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
