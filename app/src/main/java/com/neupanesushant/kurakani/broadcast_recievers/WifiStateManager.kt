package com.neupanesushant.kurakani.broadcast_recievers

object WifiStateManager {
    private var onWifiStateChange: ((Boolean) -> Unit)? = null

    fun setOnWifiStateChange(onStateChange: (Boolean) -> Unit) {
        onWifiStateChange = onStateChange
    }

    fun notifyWifiStateChanged(wifiState: Boolean) {
        onWifiStateChange?.invoke(wifiState)
    }
}