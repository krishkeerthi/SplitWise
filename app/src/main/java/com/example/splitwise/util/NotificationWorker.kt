package com.example.splitwise.util

import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Constraints
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import androidx.work.*
import com.example.splitwise.ui.activity.main.MainActivity
import java.util.concurrent.TimeUnit

class NotificationWorker : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val constraints = androidx.work.Constraints.Builder()
            //.setRequiredNetworkType(NetworkType.METERED)
            .build()


        val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicWorker>(
            15, TimeUnit.MINUTES,
            1, TimeUnit.MINUTES
        )
            // .setConstraints(constraints)
//            .setBackoffCriteria(  // retry, back off policy
//                BackoffPolicy.LINEAR,
//                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
//                TimeUnit.MILLISECONDS)
            .addTag("Periodic work tag")
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<OneTimeWorker>()
            .setConstraints(constraints)  // constraint
            .setInitialDelay(2, TimeUnit.SECONDS)  // delay
            .addTag("One time work tag")  // tag
//            .setInputData(workDataOf( // input data
//                "oneTimeData" to "Welcome to one time work",
//
//            ))
//            .setBackoffCriteria(  // retry, back off policy
//                BackoffPolicy.LINEAR,
//                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
//                TimeUnit.MILLISECONDS)
            .build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(periodicWorkRequest)


        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

    }

    class PeriodicWorker(context: Context, workerParameters: WorkerParameters) :
        Worker(context, workerParameters) {


        override fun doWork(): Result {

            val notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.sendNotification(applicationContext)

            //playMusic()

            return Result.success()
        }

        private fun playMusic(){
            MediaPlayer.create(applicationContext, com.example.splitwise.R.raw.delete).start()
        }
    }

    class OneTimeWorker(context: Context, workerParameters: WorkerParameters) :
        Worker(context, workerParameters) {


        override fun doWork(): Result {

            Log.d(TAG, "doWork: periodic work working ")
            val handler = HandlerCompat.createAsync(Looper.getMainLooper())
            handler.post {
                Toast.makeText(applicationContext, "Periodic work called",
                    Toast.LENGTH_SHORT).show()
            }

            val notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.sendNotification(applicationContext)

            playMusic()

            return Result.success()
        }

        private fun playMusic(){
            MediaPlayer.create(applicationContext, com.example.splitwise.R.raw.delete).start()
        }
    }
}