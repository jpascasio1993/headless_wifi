package com.awd.plugin.headlesswifi

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.lang.ref.WeakReference

class ServiceNotification {

    companion object {
        private const val CHANNEL_ID = "headless_wifi_channel_1001"
        private const val CHANNEL_NAME = "headless_wifi_channel"

        fun createNotification(context: WeakReference<Context>): Notification {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(context)
            }

            val packageName = context.get()!!.packageName
            val notificationIntent = context.get()!!.packageManager
                .getLaunchIntentForPackage(packageName)
            val pendingIntent = PendingIntent.getActivity(context.get()!!, 1,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE)

            return setupNotification(context, pendingIntent)
        }

        private fun setupNotification(context: WeakReference<Context>,  notificationIntent: PendingIntent): Notification  {
            // Setup notification channel and notification builder
            // This is where you would create the notification
            val notification = NotificationCompat.Builder(context.get()!!, CHANNEL_ID)
//                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("SMS Scheduler")
                .setContentText("SMS Scheduler has started")
                .setOngoing(true)
                .setContentIntent(notificationIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            notification.flags = notification.flags or NotificationCompat.FLAG_NO_CLEAR
            return notification
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannel(context: WeakReference<Context>) {
           val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Headless Wifi Service"
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.setSound(null, null)
            val notificationManager = context.get()!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}