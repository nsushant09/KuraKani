package com.neupanesushant.kurakani.koinmodules

import com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.ChatMessagingViewModel
import com.neupanesushant.kurakani.data.MessageManager
import com.neupanesushant.kurakani.activities.services.*
import com.neupanesushant.kurakani.services.CameraService
import com.neupanesushant.kurakani.services.DownloadService
import com.neupanesushant.kurakani.services.NotificationService
import com.neupanesushant.kurakani.services.ShareService
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