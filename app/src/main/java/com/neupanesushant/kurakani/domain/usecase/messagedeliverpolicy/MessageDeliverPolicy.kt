package com.neupanesushant.kurakani.domain.usecase.messagedeliverpolicy

interface MessageDeliverPolicy {
    suspend fun sendMessage(message : String)
}