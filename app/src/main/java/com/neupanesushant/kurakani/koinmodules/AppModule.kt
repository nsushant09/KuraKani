package com.neupanesushant.kurakani.koinmodules

import ShareService
import com.neupanesushant.kurakani.view.main.MainViewModel
import com.neupanesushant.kurakani.view.main.fragments.chat.ChatViewModel
import com.neupanesushant.kurakani.view.main.fragments.chatmessaging.ChatMessagingViewModel
import com.neupanesushant.kurakani.view.main.fragments.search.SearchViewModel
import com.neupanesushant.kurakani.data.MessageManager
import com.neupanesushant.kurakani.data.RegisterAndLogin
import com.neupanesushant.kurakani.data.UserManager
import com.neupanesushant.kurakani.services.cameraservice.CameraService
import com.neupanesushant.kurakani.services.downloadservice.DownloadService
import com.neupanesushant.kurakani.services.notificationservice.NotificationService
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        DownloadService(get())
    }

    single {
        ShareService(get())
    }

    single {
        CameraService(get())
    }

    single { (notificationType: NotificationService.NotificationType) ->
        NotificationService(get(), notificationType)
    }

    single {
        RegisterAndLogin()
    }

    single {
        UserManager()
    }

    factory<MessageManager> { (toId: String) ->
        MessageManager(toId)
    }

    viewModel { (friendUID: String) ->
        ChatMessagingViewModel(androidApplication(), friendUID)
    }

    viewModel {
        MainViewModel()
    }

    viewModel {
        ChatViewModel(androidApplication())
    }

    viewModel {
        SearchViewModel(androidApplication())
    }
}