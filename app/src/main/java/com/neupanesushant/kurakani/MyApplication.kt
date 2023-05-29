package com.neupanesushant.kurakani

import android.app.Application
import com.neupanesushant.kurakani.koinmodules.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}