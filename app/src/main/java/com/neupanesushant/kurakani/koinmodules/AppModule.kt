package com.neupanesushant.kurakani.koinmodules

import com.neupanesushant.kurakani.activities.services.DownloadService
import com.neupanesushant.kurakani.activities.services.NotificationService
import com.neupanesushant.kurakani.activities.services.ShareService
import org.koin.dsl.module

val appModule = module{
    single{
        DownloadService(get())
    }

    single{
        ShareService(get())
    }

    single{(notificationType : NotificationService.NotificationType)->
        NotificationService(get(), notificationType)
    }

}