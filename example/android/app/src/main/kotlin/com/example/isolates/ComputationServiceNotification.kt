// https://github.com/leancodepl/flutter-add2app-background-services/blob/main/android/app/src/main/java/co/leancode/add2appbackgroundservice/computation/ServiceNotification.kt
package com.example.isolates

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat



class ComputationServiceNotification {
    companion object {
        private const val LOG_TAG = "ServiceNotification"
        private const val TITLE_TAG = "ServiceNotificationTitle"
        private const val MESSAGE_TAG = "ServiceNotificationMessage"
        private const val PROGRESS_TAG = "ServiceNotification_Progress"
        private const val CHANNEL_ID = "ServiceNotification"
        private const val NOTIFICATION_ID = 1

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel1 = NotificationChannel(
                    CHANNEL_ID,
                    "Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
                )
                val manager: NotificationManager = context.getSystemService(
                    NotificationManager::class.java
                )
                manager.createNotificationChannel(channel1)
            }
        }

        fun createNotification(context: Context, intent: Intent, notification: String) : Notification? {
            val pendingIntent = getPendingIntentGoToForeground(context)
            intent.putExtra(TITLE_TAG, notification)

            return getBuilder(context, notification)
                .setContentIntent(pendingIntent)
                .build()
        }

        fun updateNotification(context: Context, notification: String) {
            val pendingIntent = getPendingIntentGoToForeground(context)
            val builder = getBuilder(context, notification)
                .setFullScreenIntent(pendingIntent, true)

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        }

        private fun getBuilder(context: Context, notification: String): NotificationCompat.Builder {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setOnlyAlertOnce(true)
                .setTicker(notification)

            builder.setContentTitle(notification)

            return builder
        }


        private fun getPendingIntentGoToForeground(context: Context?): PendingIntent? {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            var flags = FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= 23) {
                flags = flags or PendingIntent.FLAG_IMMUTABLE
            }

            return PendingIntent.getActivity(context, 0, intent, flags)
        }
    }
}