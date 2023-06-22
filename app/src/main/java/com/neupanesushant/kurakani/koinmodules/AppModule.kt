package com.neupanesushant.kurakani.koinmodules

import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.activities.main.fragments.chat.ChatViewModel
import com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.ChatMessagingViewModel
import com.neupanesushant.kurakani.activities.main.fragments.search.SearchViewModel
import com.neupanesushant.kurakani.data.MessageManager
import com.neupanesushant.kurakani.data.RegisterAndLogin
import com.neupanesushant.kurakani.data.UserManager
import com.neupanesushant.kurakani.services.CameraService
import com.neupanesushant.kurakani.services.DownloadService
import com.neupanesushant.kurakani.services.NotificationService
import com.neupanesushant.kurakani.services.ShareService
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
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