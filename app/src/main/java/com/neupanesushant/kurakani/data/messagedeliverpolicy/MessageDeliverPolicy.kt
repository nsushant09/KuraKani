package com.neupanesushant.kurakani.data.messagedeliverpolicy

interface MessageDeliverPolicy {
    suspend fun sendMessage(message : String)
}