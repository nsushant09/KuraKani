package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.app.Application
import android.net.Uri
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

    private var runFromOnce = false
    private var runToOnce = false

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
        }.addOnSuccessListener {
            getChatUpdateFromDatabase()
        }
    }

    fun addChatToDatabase(chatMessage: String, messageType: MessageType) {
        uiScope.launch {
            addChatToDatabaseSuspended(chatMessage, messageType)
        }
    }

    private suspend fun addChatToDatabaseSuspended(chatMessage: String, messageType: MessageType) {
        withContext(Dispatchers.IO) {
            val timeStamp = System.currentTimeMillis() / 100
            val message: Message =
                Message(firebaseAuth.uid, toId, messageType, chatMessage, timeStamp)
            FirebaseDatabase.getInstance()
                .getReference("/user-messages/$fromId$toId/$fromId$timeStamp$toId")
                .setValue(message)
            FirebaseDatabase.getInstance()
                .getReference("/user-messages/$toId$fromId/$toId$timeStamp$fromId")
                .setValue(message)
            FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
                .setValue(message)
            FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
                .setValue(message)
        }.addOnSuccessListener {
            getChatUpdateFromDatabase()
        }
    }

    fun getAllChatFromDatabase() {

        uiScope.launch {
            getAllChatSuspended()
        }
    }

    private suspend fun getAllChatSuspended() {
        withContext(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("user-messages").child("$fromId$toId")
                .get().addOnSuccessListener {
                    tempChatList.clear()
                    it.children.forEach {
                        if (it != null) {
                            val message: Message = it.getValue(Message::class.java)!!
                            tempChatList.add(0, message)
                        }
                    }
                    _chatLog.value = tempChatList
                }
        }
    }

    fun getChatUpdateFromDatabase() {
        runFromOnce = false
        runToOnce = false
        uiScope.launch {
            getChatUpdateSupended()
        }
    }

    private suspend fun getChatUpdateSupended() {
        withContext(Dispatchers.IO) {
            FirebaseDatabase.getInstance().getReference("/user-messages/$fromId$toId")
                .addChildEventListener(fromChildEventListener)
            FirebaseDatabase.getInstance().getReference("/user-messages/$toId$fromId")
                .addChildEventListener(toChildEventListener)
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

    private val fromChildEventListener = object : ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            if (!runFromOnce) {
                FirebaseDatabase.getInstance().reference.child("user-messages")
                    .child("$fromId$toId")
                    .get().addOnSuccessListener {
                        it.children.last().getValue(Message::class.java)?.let {
                            tempChatList.add(0, it)
                            _chatLog.value = tempChatList
                        }
                    }
                runFromOnce = true
            }
        }

        override fun onChildChanged(
            snapshot: DataSnapshot,
            previousChildName: String?
        ) {
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            if (!runFromOnce) {
                getAllChatFromDatabase()
                runFromOnce = true
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onCancelled(error: DatabaseError) {
        }

    }

    private val toChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            if (!runToOnce) {
                getAllChatFromDatabase()
                runToOnce = true
            }
        }

        override fun onChildChanged(
            snapshot: DataSnapshot,
            previousChildName: String?
        ) {
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            if (!runToOnce) {
                getAllChatFromDatabase()
                runToOnce = true
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onCancelled(error: DatabaseError) {
        }

    }

}
