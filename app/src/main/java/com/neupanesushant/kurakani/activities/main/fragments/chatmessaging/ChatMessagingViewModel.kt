package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.app.Application
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
import com.neupanesushant.kurakani.classes.User
import kotlinx.coroutines.*
import java.sql.Time
import java.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList

class ChatMessagingViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ChatMessagingViewModel"
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val fromId : String
    private var toId = ""


    private val _chatLog = MutableLiveData<ArrayList<Message>>()
    val chatLog get() = _chatLog

    private val tempChatList = ArrayList<Message>()

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

    fun addChatToDatabase(chatMessage : String){
        uiScope.launch{
            addChatToDatabaseSuspended(chatMessage)
        }
    }
    suspend fun addChatToDatabaseSuspended(chatMessage: String){
        withContext(Dispatchers.IO){
            val message : Message = Message(firebaseAuth.uid, toId, chatMessage, System.currentTimeMillis()/1000)
            FirebaseDatabase.getInstance().getReference("/user-messages/$fromId$toId").push().setValue(message)
            FirebaseDatabase.getInstance().getReference("/user-messages/$toId$fromId").push().setValue(message)
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
            FirebaseDatabase.getInstance().getReference().child("user-messages").child("$fromId$toId").get().addOnSuccessListener {
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

    suspend fun getChatUpdateFromDatabaseSuspended(){
        withContext(Dispatchers.IO){
            FirebaseDatabase.getInstance().getReference("/user-messages/$fromId$toId").addChildEventListener(chatUpdateChildEventListener)
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
            Log.i(TAG, "Child Changed")
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.i(TAG, "Child removed")
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            Log.i(TAG, "child moved")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i(TAG, "Cancelled : Error is " + error.toString())
        }

    }
}
