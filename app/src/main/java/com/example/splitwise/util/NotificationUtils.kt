package com.example.splitwise.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo.PRIORITY_HIGH
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.splitwise.R
import com.example.splitwise.ui.activity.main.MainActivity

private val NOTIFICAITON_ID = 0
private val NOTIFICATION_CHANNEL = "splitwise_channel"
private val NOTIFICATION_CHANNEL_NAME ="Splitwise channel"

fun NotificationManager.sendNotification(message: String, context: Context){

    val contentIntent = Intent(context, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICAITON_ID,
        contentIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(
        context,
        NOTIFICATION_CHANNEL
    ).apply {
        setSmallIcon(R.drawable.splitwiseicon)
        setContentTitle("Neenga siricha azhaga irukinga😉")
        setContentText("Apdiye selava panninga na inum azhaga irupinga😊")

        setContentIntent(contentPendingIntent)
        priority = NotificationCompat.PRIORITY_HIGH

        setAutoCancel(true)
    }

    notify(NOTIFICAITON_ID, builder.build())
}

fun NotificationManager.cancelNotifications(){
    cancelAll()
}

fun createNotificationChannel(context: Context){
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.apply {
            setShowBadge(false)
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            description ="Splitwise notification channel"

        }

        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )

        notificationManager.createNotificationChannel(channel)
    }

}