package com.neupanesushant.kurakani.domain.usecase.messaging_notification

import com.neupanesushant.kurakani.domain.model.Message

interface NotificationManager {
    fun send(message : Message)
}