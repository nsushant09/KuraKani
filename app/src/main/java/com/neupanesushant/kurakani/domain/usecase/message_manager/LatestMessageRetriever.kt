package com.neupanesushant.kurakani.domain.usecase.message_manager

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.domain.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LatestMessageRetriever() : ValueEventListener {

    val latestMessages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())

    suspend fun fetch() = coroutineScope {
        FirebaseInstance.firebaseDatabase.reference.child("latest-messages")
            .child(FirebaseInstance.fromId)
            .addValueEventListener(this@LatestMessageRetriever)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        val tempList =
            snapshot.getValue<HashMap<String, Message>>()
                ?.values
                ?.sortedByDescending { it.timeStamp }
                ?: return

        CoroutineScope(Dispatchers.IO).launch {
            latestMessages.emit(
                tempList
            )
        }
    }

    override fun onCancelled(error: DatabaseError) {
    }
}