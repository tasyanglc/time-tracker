package com.example.momentum.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.momentum.ActivityRecord
import com.example.momentum.SupabaseManager
import com.example.momentum.ui.theme.CardBorder
import com.example.momentum.ui.theme.CoffeeIconBg
import com.example.momentum.ui.theme.CoffeeIconColor
import com.example.momentum.ui.theme.ErrorContainer
import com.example.momentum.ui.theme.ManropeFontFamily
import com.example.momentum.ui.theme.OnErrorContainer
import com.example.momentum.ui.theme.OnPrimaryFixed
import com.example.momentum.ui.theme.OnSecondaryContainer
import com.example.momentum.ui.theme.OnSurface
import com.example.momentum.ui.theme.OnSurfaceVariant
import com.example.momentum.ui.theme.OnTertiaryFixed
import com.example.momentum.ui.theme.Outline
import com.example.momentum.ui.theme.Primary
import com.example.momentum.ui.theme.PrimaryContainer
import com.example.momentum.ui.theme.SecondaryContainer
import com.example.momentum.ui.theme.SurfaceContainerLowest
import com.example.momentum.ui.theme.TertiaryFixed
import com.example.momentum.ui.theme.WarmBackground
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(navController: NavController, activityId: Int) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("UserSession", Context.MODE_PRIVATE) }
    val userId = sharedPref.getString("userId", "") ?: ""
    val coroutineScope = rememberCoroutineScope()

    var record by remember { mutableStateOf<ActivityRecord?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(activityId) {
        isLoading = true
        val allActivities = SupabaseManager.getAllActivities(userId)
        record = allActivities.find { it.id == activityId }
        isLoading = false
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Activity", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete \"${record?.name ?: ""}\"? This action cannot be undone.",
                    fontFamily = ManropeFontFamily
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        coroutineScope.launch {
                            val success = SupabaseManager.deleteActivity(activityId)
                            if (success) {
                                Toast.makeText(context, "Activity deleted", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Failed to delete activity", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer, contentColor = OnErrorContainer)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Outline)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Activity Detail",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = OnSurface
                        )
                    }
                },
                actions = {
                    if (record != null) {
                        IconButton(onClick = {
                            navController.navigate("add_edit?activityId=$activityId")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Activity",
                                tint = Primary
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Activity",
                                tint = Color(0xFFE53935)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmBackground)
            )
        },
        containerColor = WarmBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                record == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Activity not found.",
                            fontFamily = ManropeFontFamily,
                            color = OnSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    ActivityDetailContent(record!!)
                }
            }
        }
    }
}

@Composable
private fun ActivityDetailContent(record: ActivityRecord) {
    val (icon, iconBg, iconTint) = remember(record.category) {
        when (record.category) {
            "Admin" -> Triple(Icons.Default.Email as ImageVector, SecondaryContainer, OnSecondaryContainer)
            "Leisure" -> Triple(Icons.Default.LocalCafe as ImageVector, CoffeeIconBg, CoffeeIconColor)
            "Health" -> Triple(Icons.Default.DirectionsRun as ImageVector, TertiaryFixed, OnTertiaryFixed)
            else -> Triple(Icons.Default.Palette as ImageVector, PrimaryContainer.copy(alpha = 0.3f), Primary)
        }
    }

    val formattedDate = remember(record.dateMillis) {
        try {
            SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(record.dateMillis)
        } catch (e: Exception) {
            "-"
        }
    }

    val formattedDuration = remember(record.duration) {
        val h = record.duration / 60
        val m = record.duration % 60
        when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header: icon + name + category chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = record.category,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column {
                Text(
                    text = record.name,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = OnSurface
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(iconBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = record.category,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = iconTint
                    )
                }
            }
        }

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                DetailInfoRow(
                    icon = Icons.Default.Folder,
                    label = "Project",
                    value = record.project
                )
                DetailInfoRow(
                    icon = Icons.Default.Timer,
                    label = "Duration",
                    value = formattedDuration
                )
                DetailInfoRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Date",
                    value = formattedDate
                )
                DetailInfoRow(
                    icon = Icons.Default.Schedule,
                    label = "Start Time",
                    value = record.startTime
                )
            }
        }

        // Notes section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Notes",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = OnSurface
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text = record.notes.ifBlank { "No notes added for this activity." },
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = if (record.notes.isBlank()) OnSurfaceVariant.copy(alpha = 0.6f) else OnSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryContainer.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Outline
            )
            Text(
                text = value,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = OnSurface
            )
        }
    }
}