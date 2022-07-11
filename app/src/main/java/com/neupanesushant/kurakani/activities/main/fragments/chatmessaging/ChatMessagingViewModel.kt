package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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


    private val _chatLog = MutableLiveData<List<Message>>()
    val chatLog get() = _chatLog

    init{
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        fromId = firebaseAuth.currentUser!!.uid
        getAllChatFromDatabase()
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
            val message : Message = Message(firebaseAuth.uid, toId, chatMessage, LocalTime.now().toString())
            val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId$toId").push()
            val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId$fromId").push()
            val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
            val toLatestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
            ref.setValue(message)
            toRef.setValue(message)
            latestMessageRef.setValue(message)
            toLatestMessageRef.setValue(message)
        }
    }

    fun getAllChatFromDatabase() {
        uiScope.launch {
            getAllChatSuspended()
        }
    }

    private suspend fun getAllChatSuspended() {
        withContext(Dispatchers.IO) {
            val ref = FirebaseDatabase.getInstance().getReference().child("user-messages").child("$fromId$toId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempList = ArrayList<Message>()
                    snapshot.children.forEach {
                        if(it!=null){
                            val message : Message = it.getValue(Message::class.java)!!
                            tempList.add(message)
                        }
                    }
                    tempList.reverse()
                    _chatLog.value = tempList.toList()

                }
                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, "Cancelled")
                }

            })
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
            if(newMessage != null){
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