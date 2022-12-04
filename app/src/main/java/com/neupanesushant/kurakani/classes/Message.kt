package com.neupanesushant.kurakani.classes

data class Message (
    val fromUid : String? = null,
    val toUid : String? = null,
    val messageType : MessageType? = null,
    val messageBody : String? = null,
    val timeStamp : Long? = null
        )

enum class MessageType{
    IMAGE,
    TEXT
}