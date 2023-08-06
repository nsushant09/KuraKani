package com.neupanesushant.kurakani.broadcast_recievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager

class WiFiBroadcastReceiver : BroadcastReceiver() {

    private var onWifiStateChange: (Boolean) -> Unit = {}

    private var wifiState: Boolean = false;
    override fun onReceive(context: Context?, intent: Intent?) {
        if (WifiManager.WIFI_STATE_CHANGED_ACTION == intent?.action) {
            onWifiStateChanged(context, intent)
        }
    }

    private fun onWifiStateChanged(context: Context?, intent: Intent?) {
        when (intent?.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
            WifiManager.WIFI_STATE_ENABLED -> {
                wifiState = true
                onWifiStateChange(true)
            }

            WifiManager.WIFI_STATE_DISABLED -> {
                wifiState = true
                onWifiStateChange(false)
            }
        }
    }

    fun setOnWifiStateChange(onStateChange: (Boolean) -> Unit) {
        onWifiStateChange = onStateChange
    }
}