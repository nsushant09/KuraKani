package com.neupanesushant.kurakani.domain

import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.text.TextUtils
import android.util.TypedValue
import android.widget.Toast
import androidx.work.impl.utils.ForceStopRunnable.BroadcastReceiver
import com.neupanesushant.kurakani.broadcast_recievers.WiFiBroadcastReceiver

object Utils {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun String.isEmptyAfterTrim(): Boolean {
        return TextUtils.isEmpty(this.trim { it <= ' ' })
    }

    fun Activity.convertToDp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }
}