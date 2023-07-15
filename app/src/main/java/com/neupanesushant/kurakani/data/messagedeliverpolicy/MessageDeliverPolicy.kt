package com.neupanesushant.kurakani.data.messagedeliverpolicy

import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.data.FirebaseInstance
import kotlinx.coroutines.launch

interface MessageDeliverPolicy {
    suspend fun sendMessage(message : String)
}