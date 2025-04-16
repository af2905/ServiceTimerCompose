package com.github.af2905.servicetimercompose

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.af2905.servicetimercompose.services.TimerService
import com.github.af2905.servicetimercompose.ui.theme.ServiceTimerComposeTheme

class MainActivity : ComponentActivity() {
    private var timerReceiver: BroadcastReceiver? = null
    private val timerState = mutableIntStateOf(0)

    private lateinit var timerServiceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        timerServiceIntent = Intent(this, TimerService::class.java)

        registerTimerReceiver()

        setContent {
            ServiceTimerComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    TimerScreen(
                        time = timerState.intValue,
                        onStart = { startService(timerServiceIntent) },
                        onStop = { stopService(timerServiceIntent) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun registerTimerReceiver() {
        val filter = IntentFilter("TIMER_UPDATED")
        timerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val time = intent?.getIntExtra("time", 0) ?: 0
                timerState.intValue = time
            }
        }
        val flag = if (Build.VERSION.SDK_INT >= 33) Context.RECEIVER_NOT_EXPORTED else 0
        registerReceiver(timerReceiver, filter, flag)

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(timerReceiver)
    }
}

@Composable
fun TimerScreen(
    time: Int,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⏱ Time elapsed: $time sec", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Row {
            Button(onClick = onStart) {
                Text("Start")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onStop) {
                Text("Stop")
            }
        }
    }
}