package com.neupanesushant.kurakani.koinmodules

import com.neupanesushant.kurakani.broadcast_recievers.WiFiBroadcastReceiver
import com.neupanesushant.kurakani.data.MessageManager
import com.neupanesushant.kurakani.data.RegisterAndLogin
import com.neupanesushant.kurakani.data.UserManager
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.domain.usecase.CameraUseCase
import com.neupanesushant.kurakani.domain.usecase.DownloadFileUseCase
import com.neupanesushant.kurakani.domain.usecase.ShareUseCase
import com.neupanesushant.kurakani.domain.usecase.message_manager.LatestMessageRetriever
import com.neupanesushant.kurakani.ui.main.MainViewModel
import com.neupanesushant.kurakani.ui.main.fragments.chat.ChatViewModel
import com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.ChatMessagingViewModel
import com.neupanesushant.kurakani.ui.main.fragments.search.SearchViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        DownloadFileUseCase(get())
    }

    single {
        ShareUseCase(get())
    }

    single {
        CameraUseCase(get())
    }

    single {
        RegisterAndLogin()
    }

    single {
        UserManager()
    }

    factory<MessageManager> { (friend: User) ->
        MessageManager(androidApplication(), friend)
    }

    single {
        WiFiBroadcastReceiver()
    }

    viewModel { (friend: User) ->
        ChatMessagingViewModel(friend)
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