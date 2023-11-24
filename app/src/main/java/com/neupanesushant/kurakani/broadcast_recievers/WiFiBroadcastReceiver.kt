package com.neupanesushant.kurakani.broadcast_recievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager

class WiFiBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (WifiManager.WIFI_STATE_CHANGED_ACTION == intent?.action) {
            onWifiStateChanged(context, intent)
        }
    }

    private fun onWifiStateChanged(context: Context?, intent: Intent?) {
        val wifiState = when (intent?.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
            WifiManager.WIFI_STATE_ENABLED -> true
            WifiManager.WIFI_STATE_DISABLED -> false
            else -> return
        }

        WifiStateManager.notifyWifiStateChanged(wifiState)
    }
}