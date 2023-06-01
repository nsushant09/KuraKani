package com.neupanesushant.kurakani.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.neupanesushant.kurakani.R

class NotificationService(private val context: Context, private val notificationType: NotificationType) {

    private val CHANNEL_ID = "channelID"
    private lateinit var builder: NotificationCompat.Builder

    init{
        when(notificationType){
            NotificationType.IMAGE -> setupImageNotification();
        }
        createNotificationChannel();

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channelName"
            val description = "channelDescription"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            // register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupImageNotification() {
        builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_kurakani_logo)
            .setContentTitle("Download")
            .setContentText("Image saved")
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }

    public fun sendNotification(){
        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }

    enum class NotificationType{
        IMAGE
    }
}