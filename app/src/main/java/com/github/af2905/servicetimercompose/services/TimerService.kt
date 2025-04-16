package com.github.af2905.servicetimercompose.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null

    private var seconds = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
}
