package com.neupanesushant.kurakani.domain.usecase.audiorecorder

import android.os.Handler
import android.os.Looper

class AutoRunningTimer {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var time = -1;

    private fun increaseTime() {
        time++
    }

    fun resetTime() {
        time = -1;
        if (this::runnable.isInitialized)
            handler.removeCallbacks(runnable)
    }

    fun getPrettyTime(callback: (String) -> Unit) {
        runnable = Runnable {
            increaseTime()
            callback(String.format("%02d : %02d", time / 60, time % 60))
            handler.postDelayed(runnable, 1000)
        }
        handler.post(runnable)
    }

}