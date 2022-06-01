package com.camerapet.debug.ui.owner

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.camerapet.debug.MainActivity
import com.camerapet.debug.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OwnerService : Service() {

    val SERVICE_NOTIFY_ID = 1
    val PUSH_ID = 2
    private var notificationManager: NotificationManager? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //TODO Make notifications more meaningful
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("channelTest", "test", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager?.createNotificationChannel(channel)
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(this, "channelTest")
                .setContentTitle("TEst")
                .setContentText("teST")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .build()
            startForeground(1, notification)
        }
        val db = Firebase.firestore
        Firebase.auth.currentUser?.uid?.let {
            db.collection("notify").document(it).addSnapshotListener { value, error ->
                if (value != null) {
                    val notificationIntent = Intent(this, MainActivity::class.java)
                    if (value.getLong("timestamp") != null) {

                        //TODO flag_immutable for api >= 23
                        val pendingIntent = PendingIntent.getActivity(
                            this,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        val notification = NotificationCompat.Builder(this, "channelTest")
                            .setContentTitle("TEst шумит")
                            .setContentText("teST")
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentIntent(pendingIntent)
                            .build()
                        notificationManager?.notify(PUSH_ID, notification)
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
