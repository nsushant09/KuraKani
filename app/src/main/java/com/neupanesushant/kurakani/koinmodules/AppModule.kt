package com.neupanesushant.kurakani.koinmodules

import android.os.Message
import com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.ChatMessagingViewModel
import com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.MessageManager
import com.neupanesushant.kurakani.activities.services.*
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val appModule = module{
    single{
        DownloadService(get())
    }

    single{
        ShareService(get())
    }

    single{
        CameraService(get())
    }

    single{(notificationType : NotificationService.NotificationType)->
        NotificationService(get(), notificationType)
    }

    factory<MessageManager>{
        (toId : String) -> MessageManager(toId)
    }

    viewModel {
        (toId : String) -> ChatMessagingViewModel(androidApplication(), get{ parametersOf(toId) })
    }
}