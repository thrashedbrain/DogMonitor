package com.camerapet.debug.ui.owner

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.camerapet.debug.MainActivity
import com.camerapet.debug.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OwnerWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    val SERVICE_NOTIFY_ID = 1
    val PUSH_ID = 2

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        val db = Firebase.firestore
        setForegroundAsync(createForegroundInfo())
        Firebase.auth.currentUser?.uid?.let {
            db.collection("notify").document(it).addSnapshotListener { value, error ->
                if (value != null) {
                    val notificationIntent = Intent(context, MainActivity::class.java)
                    if (value.getLong("timestamp") != null) {

                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        val notification = NotificationCompat.Builder(context, "channelTest")
                            .setContentTitle("TEst шумит")
                            .setContentText("teST")
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentIntent(pendingIntent)
                            .build()
                        notificationManager.notify(PUSH_ID, notification)
                    }
                }
            }
        }
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createForegroundInfo(): ForegroundInfo {
        val channel =
            NotificationChannel("channelTest", "test", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        val notification = NotificationCompat.Builder(context, "channelTest")
            .setContentTitle("TEst")
            .setContentText("teST")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .build()
        return ForegroundInfo(22, notification)
    }
}
