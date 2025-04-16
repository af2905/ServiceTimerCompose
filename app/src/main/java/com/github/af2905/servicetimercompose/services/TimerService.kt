package com.github.af2905.servicetimercompose.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null

    private var seconds = 0
    private val notificationId = 1

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun startTimer() {
        createNotificationChannel()
        startForeground(notificationId, buildNotification(seconds))

        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                seconds++

                val notification = buildNotification(seconds)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(notificationId, notification)
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        resetTimer()

        stopForeground(STOP_FOREGROUND_REMOVE)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(notificationId)
    }


    fun getSeconds(): Int = seconds

    fun isTimerRunning(): Boolean = timerJob?.isActive == true

    private fun resetTimer() {
        seconds = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        resetTimer()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopTimer()
        stopSelf()
        return super.onUnbind(intent)
    }


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
