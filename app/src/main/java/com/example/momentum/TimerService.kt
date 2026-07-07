package com.example.momentum

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.momentum.ui.screens.TimerState
import kotlinx.coroutines.*
import java.util.*

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1002
        
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
            "ACTION_STOP_AND_SAVE" -> stopAndSave()
        }
        return START_STICKY
    }

    private fun stopAndSave() {
        serviceScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    val userId = sharedPref.getString("userId", "") ?: ""

                    if (userId.isNotEmpty() && TimerState.secondsElapsed > 0) {
                        val durationMins = (TimerState.secondsElapsed / 60).coerceAtLeast(1).toInt()
                        val taskName = TimerState.currentTaskName.trim().ifEmpty { "Active Session" }
                        val projectSelected = TimerState.currentProject

                        val category = when {
                            taskName.contains("mail", true) || taskName.contains("admin", true) || taskName.contains("sync", true) -> "Admin"
                            taskName.contains("break", true) || taskName.contains("coffee", true) || taskName.contains("relax", true) -> "Leisure"
                            taskName.contains("health", true) || taskName.contains("run", true) || taskName.contains("gym", true) -> "Health"
                            else -> "Focus"
                        }

                        val currentMillis = System.currentTimeMillis()
                        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                        val startCal = java.util.Calendar.getInstance().apply {
                            timeInMillis = currentMillis - (TimerState.secondsElapsed * 1000)
                        }
                        val startTime = sdf.format(startCal.time)

                        SupabaseManager.addActivity(
                            ActivityRecordDto(
                                user_id = userId,
                                activity_name = taskName,
                                project = projectSelected,
                                category = category,
                                duration = durationMins,
                                notes = "Logged via Notification",
                                date_millis = currentMillis,
                                start_time = startTime
                            )
                        )
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            if (success) {
                android.widget.Toast.makeText(this@TimerService, "Session saved successfully", android.widget.Toast.LENGTH_SHORT).show()
            } else if (TimerState.secondsElapsed > 0) {
                android.widget.Toast.makeText(this@TimerService, "Database error: check your connection", android.widget.Toast.LENGTH_SHORT).show()
            }
            
            stopTimer()
        }
    }

    private fun startTimer() {
        TimerState.isRunning = true
        TimerState.isPaused = false
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(NOTIFICATION_ID, createNotification(), type)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        runTimer()
    }

    private fun pauseTimer() {
        TimerState.isPaused = true
        timerJob?.cancel()
        updateNotification()
    }

    private fun resumeTimer() {
        TimerState.isPaused = false
        runTimer()
        updateNotification()
    }

    private fun stopTimer() {
        TimerState.isRunning = false
        TimerState.isPaused = false
        TimerState.secondsElapsed = 0L
        TimerState.currentTaskName = ""
        TimerState.currentProject = ""
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                if (!TimerState.isPaused) {
                    TimerState.secondsElapsed++
                }
                updateNotification()
            }
        }
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pauseIntent = Intent(this, TimerActionReceiver::class.java).apply { action = ACTION_PAUSE }
        val pausePendingIntent = PendingIntent.getBroadcast(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val resumeIntent = Intent(this, TimerActionReceiver::class.java).apply { action = ACTION_RESUME }
        val resumePendingIntent = PendingIntent.getBroadcast(this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, TimerActionReceiver::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getBroadcast(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val timeString = formatElapsedTime(TimerState.secondsElapsed)
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(if (TimerState.isPaused) "Timer Paused" else "Timer Running")
            .setContentText("${TimerState.currentTaskName} - $timeString")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (TimerState.isPaused) {
            builder.addAction(R.drawable.ic_play, "Resume", resumePendingIntent)
        } else {
            builder.addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)
        }
        
        builder.addAction(R.drawable.ic_stop, "End", stopPendingIntent)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun formatElapsedTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
