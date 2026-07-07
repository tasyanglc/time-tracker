package com.example.momentum

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val serviceIntent = Intent(context, TimerService::class.java)

        when (action) {
            TimerService.ACTION_PAUSE -> {
                serviceIntent.action = TimerService.ACTION_PAUSE
                context.startService(serviceIntent)
            }
            TimerService.ACTION_RESUME -> {
                serviceIntent.action = TimerService.ACTION_RESUME
                context.startService(serviceIntent)
            }
            TimerService.ACTION_STOP -> {
                // Now we tell the service to stop AND save
                serviceIntent.action = "ACTION_STOP_AND_SAVE"
                context.startService(serviceIntent)
            }
        }
    }
}
