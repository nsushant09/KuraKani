//package com.neupanesushant.kurakani.domain.usecase.message_manager
//
//import com.google.firebase.database.ChildEventListener
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.neupanesushant.kurakani.domain.model.Message
//
//class ChatChildEventListener : ChildEventListener {
//    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//        val message = snapshot.getValue(Message::class.java) ?: return
//        val currentMessages = messages.value.toMutableList()
//        currentMessages.add(message)
//        messages.value = currentMessages
//    }
//
//    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//    }
//
//    override fun onChildRemoved(snapshot: DataSnapshot) {
//        val currentMessages = messages.value.toMutableList()
//        currentMessages.remove(snapshot.getValue(Message::class.java))
//        messages.value = currentMessages
//    }
//
//    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//    }
//
//    override fun onCancelled(error: DatabaseError) {
//    }
//}