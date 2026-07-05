package com.example.momentum.ui.screens

import android.content.Context
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.momentum.NotificationHelper
import com.example.momentum.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE) }

    var isReminderEnabled by remember {
        mutableStateOf(sharedPref.getBoolean("reminders_enabled", false))
    }
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
        } else {
            isReminderEnabled = false
            sharedPref.edit { putBoolean("reminders_enabled", false) }
        }
    }

    // Function to check and request Exact Alarm permission for Android 12+
    val checkExactAlarmPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    }

    var reminderHour by remember {
        mutableIntStateOf(sharedPref.getInt("reminder_hour", 9))
    }
    var reminderMinute by remember {
        mutableIntStateOf(sharedPref.getInt("reminder_minute", 0))
    }

    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notification Settings",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmBackground,
                    titleContentColor = OnSurface
                )
            )
        },
        containerColor = WarmBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Customize how you want to be reminded to track your time.",
                fontFamily = ManropeFontFamily,
                fontSize = 14.sp,
                color = Outline
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Daily Reminders",
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = OnSurface
                            )
                            Text(
                                "Receive a notification to log your activities.",
                                fontFamily = ManropeFontFamily,
                                fontSize = 12.sp,
                                color = Outline
                            )
                        }
                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                    checkExactAlarmPermission()
                                }

                                isReminderEnabled = enabled
                                sharedPref.edit { putBoolean("reminders_enabled", enabled) }
                                if (enabled) {
                                    NotificationHelper.scheduleReminder(context, reminderHour, reminderMinute)
                                } else {
                                    NotificationHelper.cancelReminder(context)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Primary,
                                checkedTrackColor = PrimaryContainer
                            )
                        )
                    }

                    if (isReminderEnabled) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = Outline.copy(alpha = 0.1f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Reminder Time",
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = OnSurface
                            )
                            
                            val timeText = String.format(java.util.Locale.getDefault(), "%02d:%02d", reminderHour, reminderMinute)
                            TextButton(onClick = { showTimePicker = true }) {
                                Text(
                                    timeText,
                                    fontFamily = ManropeFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }
            
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Primary
                    )
                    Text(
                        "Reminders help you maintain a consistent tracking habit and ensure your reports are accurate.",
                        fontFamily = ManropeFontFamily,
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderHour,
            initialMinute = reminderMinute
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    reminderHour = timePickerState.hour
                    reminderMinute = timePickerState.minute
                    sharedPref.edit {
                        putInt("reminder_hour", reminderHour)
                        putInt("reminder_minute", reminderMinute)
                    }
                    
                    if (isReminderEnabled) {
                        NotificationHelper.scheduleReminder(context, reminderHour, reminderMinute)
                    }
                    showTimePicker = false
                }) {
                    Text("OK", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = Outline)
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}