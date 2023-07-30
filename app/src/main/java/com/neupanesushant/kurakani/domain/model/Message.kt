package com.neupanesushant.kurakani.domain.model

import java.io.Serializable

data class Message (
    val fromUid : String? = null,
    val toUid : String? = null,
    val messageType : MessageType? = null,
    val messageBody : String? = null,
    val timeStamp : Long? = null
        ) : Serializable

enum class MessageType{
    TEXT,
    IMAGE,
    AUDIO
}