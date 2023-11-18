package com.neupanesushant.kurakani.domain.usecase.message_manager

import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.domain.model.User

class MessageDeleter(private val friend: User) {
    fun delete(timeStamp: String) {
        val fromId = FirebaseInstance.fromId
        FirebaseInstance.firebaseDatabase
            .getReference("/user-messages/$fromId${friend.uid}/$fromId$timeStamp${friend.uid}")
            .removeValue()
    }
}