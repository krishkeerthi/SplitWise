package com.example.splitwise.firebase

import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.splitwise.util.sendCustomNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        //Toast.makeText(this, "${message.data["title"]} ${message.data["message"]}", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onMessageReceived: ${message.data["title"]} ${message.data["body"]}")

        val notificationManager = ContextCompat.getSystemService(
            baseContext,
            NotificationManager::class.java
        ) as NotificationManager

        notificationManager.sendCustomNotification(
            baseContext,
            message.data["title"].toString(),
            message.data["body"].toString()
        )

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: $token")
    }
}