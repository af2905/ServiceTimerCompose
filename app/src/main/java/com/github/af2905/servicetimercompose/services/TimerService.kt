package com.github.af2905.servicetimercompose.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null

    private var seconds = 0
    private val notificationId = 1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(1, buildNotification(seconds))
        startTimer()
        return START_STICKY
    }


    private fun startTimer() {
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                seconds++

                val broadcastIntent = Intent("TIMER_UPDATED")
                broadcastIntent.putExtra("time", seconds)
                sendBroadcast(broadcastIntent)

                val notification = buildNotification(seconds)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(notificationId, notification)
            }
        }
    }

    private fun resetTimer() {
        seconds = 0
        val resetIntent = Intent("TIMER_UPDATED")
        resetIntent.putExtra("time", seconds)
        sendBroadcast(resetIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        resetTimer()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "timer_channel",
            "Timer Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(seconds: Int): Notification {
        return NotificationCompat.Builder(this, "timer_channel")
            .setContentTitle("Timer Running")
            .setContentText("Elapsed time: $seconds sec")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }
}
