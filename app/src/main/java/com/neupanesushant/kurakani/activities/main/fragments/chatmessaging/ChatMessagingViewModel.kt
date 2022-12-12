package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType
import com.neupanesushant.kurakani.classes.User
import kotlinx.coroutines.*
import java.sql.Time
import java.time.LocalTime
import java.util.*
import java.util.Map
import kotlin.collections.ArrayList

class ChatMessagingViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ChatMessagingViewModel"
    private var firebaseUser: FirebaseUser
    private var firebaseAuth: FirebaseAuth
    private var firebaseStorage: FirebaseStorage

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val fromId : String
    private var toId = ""


    private val _chatLog = MutableLiveData<ArrayList<Message>>()
    val chatLog get() = _chatLog

    private val tempChatList = ArrayList<Message>()

    private val _sentImagesList = MutableLiveData<kotlin.collections.LinkedHashMap<String, Uri?>>()
    private val sentImagesList get() = _sentImagesList
    private var sentImageUploadCounter = 0

    init{
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        fromId = firebaseAuth.currentUser!!.uid
        getChatUpdateFromDatabase()
    }

    fun setToID(uid : String){
        toId = uid
    }

    fun deleteChatFromDatabase(timeStamp : String){
        uiScope.launch{
            deleteChatFromDatabaseSuspended(timeStamp)
        }
    }

    suspend fun deleteChatFromDatabaseSuspended(timeStamp : String){
        withContext(Dispatchers.IO){
            FirebaseDatabase.getInstance().getReference("/user-messages/$fromId$toId/$fromId$timeStamp$toId").removeValue()
        }
    }

    fun addChatToDatabase(chatMessage : String, messageType : MessageType){
        uiScope.launch{
            addChatToDatabaseSuspended(chatMessage, messageType)
        }
    }
    suspend fun addChatToDatabaseSuspended(chatMessage: String, messageType: MessageType){
        withContext(Dispatchers.IO){
            val timeStamp = System.currentTimeMillis() / 100
            val message : Message = Message(firebaseAuth.uid, toId, messageType,chatMessage, timeStamp)
            FirebaseDatabase.getInstance().getReference("/user-messages/$fromId$toId/$fromId$timeStamp$toId").setValue(message)
            FirebaseDatabase.getInstance().getReference("/user-messages/$toId$fromId/$toId$timeStamp$fromId").setValue(message)
            FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId").setValue(message)
            FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId").setValue(message)
        }
    }

    fun getAllChatFromDatabase() {
        uiScope.launch {
            getAllChatSuspended()
        }
    }

    private suspend fun getAllChatSuspended() {
        withContext(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("user-messages").child("$fromId$toId").get().addOnSuccessListener {
                tempChatList.clear()
                it.children.forEach{
                    if(it!=null){
                        val message : Message = it.getValue(Message::class.java)!!
                        tempChatList.add(0, message)
                    }
                }
                _chatLog.value = tempChatList
            }
        }
    }

    fun getChatUpdateFromDatabase(){
        uiScope.launch{
            getChatUpdateFromDatabaseSuspended()
        }
    }

    private suspend fun getChatUpdateFromDatabaseSuspended(){
        withContext(Dispatchers.IO){
            FirebaseDatabase.getInstance().getReference("/user-messages/$fromId$toId").addChildEventListener(chatUpdateChildEventListener)
            FirebaseDatabase.getInstance().getReference("/user-messages/$toId$fromId").addChildEventListener(chatUpdateChildEventListener)
        }
    }

    fun addImagesToDatabase(imageList : kotlin.collections.LinkedHashMap<String, Uri?>){
        sentImageUploadCounter = 0
        _sentImagesList.value = imageList
        if(sentImagesList.value != null){
            addSingleImageToDatabase(sentImageUploadCounter)
        }

    }

    private fun addSingleImageToDatabase(index : Int){
        val keys = sentImagesList.value!!.keys
        if(index < keys.size){
            val currentKey = keys.elementAt(index)
            val ref = firebaseStorage.getReference("/images/${currentKey}")
            ref.putFile(sentImagesList.value!!.get(currentKey)!!).addOnSuccessListener {
                addSingleImageToDatabase(index + 1)
                ref.downloadUrl.addOnSuccessListener{
                    addChatToDatabase(it.toString(), MessageType.IMAGE)
                    getChatUpdateFromDatabase()
                }
            }
        }

    }

    private val chatUpdateChildEventListener = object : ChildEventListener{
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val newMessage = snapshot.getValue(Message::class.java)
            if(newMessage != null ){
                getAllChatFromDatabase()
            }

        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            getAllChatFromDatabase()
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            getAllChatFromDatabase()
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            Log.i(TAG, "child moved")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i(TAG, "Cancelled : Error is " + error.toString())
        }

    }
}
