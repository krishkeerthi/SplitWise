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
private val NOTIFICATION_CHANNEL_NAME = "Splitwise channel"

private val notificationTitle = listOf(
    "Neenga siricha azhaga irukinga 😉",
    "Natpu na enna theriyuma Surya na enna theriyuma 👨‍❤️‍👨",
    "(1997/4 + 598/3 + 7650/12 + 3160/8) = ?  🤔",
    "1 🍌 inga iruku innonu 🍌 enga 🧐",
    "Naalu kasu sambadhikanum na savanum 😵",
    "Odra bus 🚎 la irundhu 1 rupee coin 🪙 vizhundhale",
    "Thakkali 🍅 kilo 8 ruba, Vengayam 🧅 kilo 15 ruba",
    "Avalai maraka thaan 💑 ennai maraka thaan ❤️‍🩹",
    "Marakuma nenjam 😵‍💫",
    "Kasukoru panjam vandhalum \uD83D\uDCB0"

)

private val notificationBody = listOf(
    "Aana selavu panninga na inum azhaga irupinga \uD83D\uDE0A Aprom marakama split potrunga \uD83E\uDD11",
    "Oru expense split podunga \uD83D\uDCB0 Aprom ellam theriyum \uD83D\uDE02",
    "Ungaluku yen kastam, use splitwise and be wise\uD83D\uDE1C",
    "Ipdi ungala yarum yematha mudiyathu, Splitwise use pannunga \uD83D\uDCB5 kanaku sariya irukum ✅",
    "Sambadhichcha kasu veetuku kondu varanum na raththa adi padanum\uD83E\uDE78. Adhanala porupa Splitwise use pannunga",
    "urundu vandhu edukum JP kooda Splitwise dha use pannaru\uD83D\uDE02",
    "Kanaku pathu selavu pandravangala neenga, appo splitwise kandipa use agum \uD83E\uDD23",
    "Adhuku dha solren\uD83D\uDE02 splitwise use pannunga, edhuvum marakadhu",
    "Kandipa marakum, olunga splitwise use pannunga\uD83E\uDD23",
    "\"Pasathuku panjam illanu solirupangale\", nambadhinga - Splitwise"
)

fun NotificationManager.sendNotification(context: Context) {

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
        val index = (0..9).random()
        setContentTitle(notificationTitle[index])
        setContentText(notificationBody[index])

        setContentIntent(contentPendingIntent)
        priority = NotificationCompat.PRIORITY_HIGH

        setAutoCancel(true)
    }

    notify(NOTIFICAITON_ID, builder.build())
}

fun NotificationManager.sendCustomNotification(context: Context, notificationTitle: String, notificationBody: String) {

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
        setContentTitle(notificationTitle)
        setContentText(notificationBody)

        setContentIntent(contentPendingIntent)
        priority = NotificationCompat.PRIORITY_HIGH

        setAutoCancel(true)
    }

    notify(NOTIFICAITON_ID, builder.build())
}

fun NotificationManager.welcomeNotification(context: Context) {

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
        setContentTitle("Neenga siricha azhaga irukinga \uD83D\uDE09")
        setContentText("Aana selavu panninga na inum azhaga irupinga \uD83D\uDE0A Aprom marakama split potrunga \uD83E\uDD11")

        setContentIntent(contentPendingIntent)
        priority = NotificationCompat.PRIORITY_HIGH

        setAutoCancel(true)
    }

    notify(NOTIFICAITON_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            description = "Splitwise notification channel"

        }

        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )

        notificationManager.createNotificationChannel(channel)
    }

}