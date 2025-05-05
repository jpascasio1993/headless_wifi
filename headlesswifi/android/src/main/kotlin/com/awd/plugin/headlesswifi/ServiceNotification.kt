package com.awd.plugin.headlesswifi

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class ServiceNotification {

    companion object {
        private const val CHANNEL_ID = "headless_wifi_channel_1001"
        private const val CHANNEL_NAME = "headless_wifi_channel"

        fun createNotification(context: Context, contextText: String?): Notification {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(context)
            }

            val packageName = context.packageName
            val notificationIntent = context.packageManager
                .getLaunchIntentForPackage(packageName)
            val pendingIntent = PendingIntent.getActivity(context, 1,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE)

            return setupNotification(context, pendingIntent, contextText)
        }
        
        private fun setupNotification(context: Context, notificationIntent: PendingIntent, contextText: String?): Notification  {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Headless Wifi")
                .setContentText(contextText ?: "Headless Wifi has started")
                .setOngoing(true)
                .setContentIntent(notificationIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            notification.flags = notification.flags or NotificationCompat.FLAG_NO_CLEAR
            return notification
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannel(context: Context) {
           val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Headless Wifi Service"
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.setSound(null, null)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}