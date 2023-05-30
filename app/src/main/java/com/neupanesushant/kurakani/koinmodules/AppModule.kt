package com.neupanesushant.kurakani.koinmodules

import com.neupanesushant.kurakani.activities.services.*
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
}