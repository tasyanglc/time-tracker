package com.example.timetracker.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.timetracker.ActivityRecord
import com.example.timetracker.DatabaseHelper
import com.example.timetracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay
import com.example.timetracker.SupabaseManager
import com.example.timetracker.ActivityRecordDto
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

// DATA CLASSES
data class ActivityItem(
    val id: Int,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTintColor: Color,
    val title: String,
    val timeRange: String,
    val duration: String,
)

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

// ACTIVE TIMER STATE HOLDER
object TimerState {
    var isRunning by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    var secondsElapsed by mutableStateOf(0L)
    var currentTaskName by mutableStateOf("")
    var currentProject by mutableStateOf("")
}

// HOME SCREEN (DASHBOARD CONTAINER)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("UserSession", Context.MODE_PRIVATE) }
    val userId = sharedPref.getString("userId", "") ?: ""

    val scrollState = rememberScrollState()
    var selectedNavItem by rememberSaveable { mutableIntStateOf(0) }

    // Ticker logic for global timer
    LaunchedEffect(TimerState.isRunning, TimerState.isPaused) {
        if (TimerState.isRunning && !TimerState.isPaused) {
            while (true) {
                delay(1000)
                TimerState.secondsElapsed++
            }
        }
    }

    // Refresh triggers to refresh database lists when screens change
    var refreshTrigger by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            val titles = listOf("Home", "History", "Start", "Reports", "Settings")
            val currentTitle = titles.getOrElse(selectedNavItem) { "Home" }
            MomentumTopBar(title = currentTitle)
        },
        bottomBar = {
            MomentumBottomNav(
                selectedIndex = selectedNavItem,
                onItemSelected = { selectedNavItem = it }
            )
        },
        floatingActionButton = {
            if (selectedNavItem == 0) {
                AddFab(onClick = { navController.navigate("add_edit") })
            }
        },
        containerColor = WarmBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Navigation routing for internal tabs ──
            when (selectedNavItem) {
                0 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    TodaySummaryCard(userId, refreshTrigger)
                    ActiveTimerWidget(onTimerAction = { selectedNavItem = 2 })
                    RecentActivitiesSection(
                        userId = userId,
                        refreshTrigger = refreshTrigger,
                        navController = navController,
                        onActivityChanged = { refreshTrigger++ }
                    )
                }
                1 -> HistoryTabContent(userId, navController)
                2 -> PlayTabContent(userId, onSaved = {
                    refreshTrigger++
                    selectedNavItem = 0
                })
                3 -> ReportsTabContent(userId)
                4 -> SettingsTabContent(navController)
            }
        }
    }
}

// TOP APP BAR
@Composable
fun MomentumTopBar(title: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("UserSession", Context.MODE_PRIVATE) }
    val username = sharedPref.getString("username", "User") ?: "User"
    val initial = if (username.isNotEmpty()) username.take(1).uppercase() else "U"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(WarmBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = OnSurface,
                letterSpacing = (-0.3).sp,
            )
        }

        // Right: Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = OnPrimaryFixed,
            )
        }
    }
}

// TODAY'S SUMMARY CARD
@Composable
fun TodaySummaryCard(userId: String, refreshTrigger: Int) {
    var todayRecords by remember { mutableStateOf<List<ActivityRecord>>(emptyList()) }

    LaunchedEffect(refreshTrigger) {
        todayRecords = SupabaseManager.getActivitiesByDate(userId, System.currentTimeMillis())
    }

    val totalMinutes = todayRecords.sumOf { it.duration }
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60

    // Daily goal is 8 hours (480 minutes)
    val percentage = if (totalMinutes > 0) (totalMinutes * 100 / 480).coerceAtMost(100) else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x0D4A3F10),
                ambientColor = Color(0x0D4A3F10),
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceContainerLowest,
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = CardBorder.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "TODAY'S SUMMARY",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 0.6.sp,
                        color = Outline,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${hours}h ${mins}m",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        lineHeight = 40.sp,
                        letterSpacing = (-0.64).sp,
                        color = OnSurface,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Stats",
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Goal: 8h",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                )
                Text(
                    text = "$percentage%",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Primary,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SurfaceContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentage / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(PrimaryContainer)
                )
            }
        }
    }
}

// ACTIVE TIMER WIDGET (HOME SHORTCUT)
@Composable
fun ActiveTimerWidget(onTimerAction: () -> Unit) {
    val timeString = formatElapsedTime(TimerState.secondsElapsed)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color(0x1AFFD541),
                ambientColor = Color(0x1AFFD541),
            )
            .clip(RoundedCornerShape(32.dp))
            .background(PrimaryContainer)
            .clickable { onTimerAction() }
    ) {
        // Decorative background element
        Box(
            modifier = Modifier
                .size(128.dp)
                .align(Alignment.TopEnd)
                .offset(x = 48.dp, y = (-48).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (TimerState.isRunning) "Timer Active: ${TimerState.currentTaskName}" else "Start Tracking Time",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = OnPrimaryFixed,
                )
            }

            Text(
                text = timeString,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                lineHeight = 52.sp,
                letterSpacing = (-1.5).sp,
                color = OnPrimaryFixed,
                textAlign = TextAlign.Center,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (TimerState.isRunning) {
                    // Pause/Resume Button
                    TimerControlButton(
                        size = 48,
                        bgColor = Color.White.copy(alpha = 0.3f),
                        onClick = { TimerState.isPaused = !TimerState.isPaused }
                    ) {
                        Icon(
                            imageVector = if (TimerState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (TimerState.isPaused) "Resume" else "Pause",
                            tint = OnPrimaryFixed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Open Timer Screen directly
                    Text(
                        text = "Manage Timer",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = OnPrimaryFixed,
                        modifier = Modifier.clickable { onTimerAction() }
                    )
                } else {
                    // Fast Launch Button
                    TimerControlButton(
                        size = 56,
                        bgColor = OnPrimaryFixed,
                        onClick = {
                            TimerState.isRunning = true
                            TimerState.isPaused = false
                            TimerState.secondsElapsed = 0
                            onTimerAction()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimerControlButton(
    size: Int,
    bgColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// RECENT ACTIVITIES SECTION
@Composable
fun RecentActivitiesSection(
    userId: String,
    refreshTrigger: Int,
    navController: NavController,
    onActivityChanged: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var records by remember { mutableStateOf<List<ActivityRecord>>(emptyList()) }

    LaunchedEffect(refreshTrigger) {
        records = SupabaseManager.getAllActivities(userId).takeLast(3).reversed()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activities",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.24).sp,
                color = OnSurface,
            )
        }

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No activities logged yet.",
                    fontFamily = ManropeFontFamily,
                    color = OnSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                records.forEach { record ->
                    val icon = when (record.category) {
                        "Admin" -> Icons.Outlined.Email
                        "Leisure" -> Icons.Outlined.LocalCafe
                        "Health" -> Icons.Outlined.DirectionsRun
                        else -> Icons.Outlined.Palette
                    }
                    val iconBg = when (record.category) {
                        "Admin" -> SecondaryContainer
                        "Leisure" -> CoffeeIconBg
                        "Health" -> TertiaryFixed
                        else -> PrimaryContainer.copy(alpha = 0.3f)
                    }
                    val iconTint = when (record.category) {
                        "Admin" -> OnSecondaryContainer
                        "Leisure" -> CoffeeIconColor
                        "Health" -> OnTertiaryFixed
                        else -> Primary
                    }

                    key(record.id) {
                        ActivityCard(
                            activity = ActivityItem(
                                id = record.id,
                                icon = icon,
                                iconBgColor = iconBg,
                                iconTintColor = iconTint,
                                title = record.name,
                                timeRange = "${record.startTime} • ${record.project}",
                                duration = "${record.duration}m"
                            ),
                            onClick = {
                                navController.navigate("add_edit?activityId=${record.id}")
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    val success = SupabaseManager.deleteActivity(record.id)
                                    if (success) {
                                        Toast.makeText(context, "Activity deleted", Toast.LENGTH_SHORT).show()
                                        onActivityChanged()
                                    } else {
                                        Toast.makeText(context, "Failed to delete activity", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCard(
    activity: ActivityItem,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteDialog = true
            }
            // Selalu return false agar kartu otomatis kembali ke posisi semula;
            // penghapusan sesungguhnya baru terjadi setelah dikonfirmasi lewat dialog.
            false
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Activity", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete \"${activity.title}\"? This action cannot be undone.",
                    fontFamily = ManropeFontFamily
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
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

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.fillMaxWidth(),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE53935))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Activity",
                    tint = Color.White
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0x0D4A3F10),
                    ambientColor = Color(0x0D4A3F10),
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceContainerLowest,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(activity.iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = activity.icon,
                            contentDescription = activity.title,
                            tint = activity.iconTintColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = activity.title,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = OnSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = activity.timeRange,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Outline,
                        )
                    }
                }

                Text(
                    text = activity.duration,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                )
            }
        }
    }
}

// HISTORY TAB CONTENT
@Composable
fun HistoryTabContent(userId: String, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val calendar = remember { Calendar.getInstance() }
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }

    val selectedCal = remember(selectedDate) {
        Calendar.getInstance().apply { timeInMillis = selectedDate }
    }

    val currentMonthYear = remember(selectedDate) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(selectedCal.time)
    }

    val dayList = remember(selectedDate) {
        // Generate list of days in selected date's month
        val tempCal = selectedCal.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        (1..maxDays).map { day ->
            val dayCal = tempCal.clone() as Calendar
            dayCal.set(Calendar.DAY_OF_MONTH, day)
            dayCal.timeInMillis
        }
    }

    var records by remember { mutableStateOf<List<ActivityRecord>>(emptyList()) }
    var historyRefreshTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(selectedDate, historyRefreshTrigger) {
        records = SupabaseManager.getActivitiesByDate(userId, selectedDate)
    }
    val totalTime = records.sumOf { it.duration }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Month Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val temp = Calendar.getInstance().apply { timeInMillis = selectedDate }
                temp.add(Calendar.MONTH, -1)
                selectedDate = temp.timeInMillis
            }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Prev", tint = Primary)
            }

            Text(
                text = currentMonthYear,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = OnSurface
            )

            IconButton(onClick = {
                val temp = Calendar.getInstance().apply { timeInMillis = selectedDate }
                temp.add(Calendar.MONTH, 1)
                selectedDate = temp.timeInMillis
            }) {
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next", tint = Primary)
            }
        }

        // Horizontal Scrollable Calendar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .verticalScroll(rememberScrollState()) // Allow simple wrapping or row, but we can do horizontal grid
        ) {
            // Display days of the week in a simpler list: showing 7 days around selected date
            val centerCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            val centerDay = centerCal.get(Calendar.DAY_OF_MONTH)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Show 7 days around the selected date
                val startCal = centerCal.clone() as Calendar
                startCal.add(Calendar.DAY_OF_MONTH, -3)

                (0..6).forEach { _ ->
                    val time = startCal.timeInMillis
                    val dayNum = startCal.get(Calendar.DAY_OF_MONTH)
                    val isSelected = isSameDay(time, selectedDate)

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PrimaryContainer else Color.Transparent)
                            .clickable { selectedDate = time },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayNum.toString(),
                            fontFamily = ManropeFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnPrimaryFixed else OnSurface,
                            fontSize = 14.sp
                        )
                    }
                    startCal.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
        }

        // Total Time Logged
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryContainer.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Time Logged",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Primary
                )
                Text(
                    text = "${totalTime / 60}h ${totalTime % 60}m",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Primary
                )
            }
        }

        // Activities List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (records.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No activities on this day.",
                            fontFamily = ManropeFontFamily,
                            color = OnSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(records, key = { it.id }) { record ->
                    val icon = when (record.category) {
                        "Admin" -> Icons.Outlined.Email
                        "Leisure" -> Icons.Outlined.LocalCafe
                        "Health" -> Icons.Outlined.DirectionsRun
                        else -> Icons.Outlined.Palette
                    }
                    val iconBg = when (record.category) {
                        "Admin" -> SecondaryContainer
                        "Leisure" -> CoffeeIconBg
                        "Health" -> TertiaryFixed
                        else -> PrimaryContainer.copy(alpha = 0.3f)
                    }
                    val iconTint = when (record.category) {
                        "Admin" -> OnSecondaryContainer
                        "Leisure" -> CoffeeIconColor
                        "Health" -> OnTertiaryFixed
                        else -> Primary
                    }

                    ActivityCard(
                        activity = ActivityItem(
                            id = record.id,
                            icon = icon,
                            iconBgColor = iconBg,
                            iconTintColor = iconTint,
                            title = record.name,
                            timeRange = "${record.startTime} • ${record.project}",
                            duration = "${record.duration}m"
                        ),
                        onClick = {
                            navController.navigate("add_edit?activityId=${record.id}")
                        },
                        onDelete = {
                            coroutineScope.launch {
                                val success = SupabaseManager.deleteActivity(record.id)
                                if (success) {
                                    Toast.makeText(context, "Activity deleted", Toast.LENGTH_SHORT).show()
                                    historyRefreshTrigger++
                                } else {
                                    Toast.makeText(context, "Failed to delete activity", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// PLAY / START TIMER TAB CONTENT
@Composable
fun PlayTabContent(userId: String, onSaved: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isProjectDropdownExpanded by remember { mutableStateOf(false) }

    var projectList by remember { mutableStateOf<List<String>>(emptyList()) }
    var refreshProjectTrigger by remember { mutableStateOf(0) }
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var projectToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshProjectTrigger) {
        projectList = SupabaseManager.getAllProjects()
        if (TimerState.currentProject.isEmpty() && projectList.isNotEmpty()) {
            TimerState.currentProject = projectList.first()
        }
    }

    val timeString = formatElapsedTime(TimerState.secondsElapsed)

    // AlertDialog for Adding New Project
    if (showAddProjectDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddProjectDialog = false
                newProjectName = ""
            },
            title = { Text("Add New Project", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newProjectName,
                    onValueChange = { newProjectName = it },
                    label = { Text("Project Name", fontFamily = ManropeFontFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newProjectName.trim()
                        if (trimmed.isNotEmpty()) {
                            coroutineScope.launch {
                                val res = SupabaseManager.addProject(trimmed)
                                if (res) {
                                    refreshProjectTrigger++
                                    showAddProjectDialog = false
                                    newProjectName = ""
                                    Toast.makeText(context, "Project added successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Project already exists or error occurred", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showAddProjectDialog = false
                        newProjectName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Outline)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // AlertDialog for Confirming Project Deletion
    projectToDelete?.let { proj ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete the project \"$proj\"?", fontFamily = ManropeFontFamily) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val deleted = SupabaseManager.deleteProject(proj)
                            if (deleted) {
                                refreshProjectTrigger++
                                if (TimerState.currentProject == proj) {
                                    TimerState.currentProject = if (projectList.size > 1) {
                                        projectList.filter { it != proj }.first()
                                    } else {
                                        ""
                                    }
                                }
                                Toast.makeText(context, "Project deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to delete project", Toast.LENGTH_SHORT).show()
                            }
                            projectToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer)
                ) {
                    Text("Delete", color = OnErrorContainer)
                }
            },
            dismissButton = {
                Button(
                    onClick = { projectToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Outline)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Timer Display
        Card(
            modifier = Modifier
                .size(240.dp)
                .shadow(elevation = 6.dp, shape = CircleShape, spotColor = PrimaryContainer),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(4.dp, PrimaryContainer)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeString,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 40.sp,
                        color = OnSurface
                    )
                    Text(
                        text = if (TimerState.isRunning && !TimerState.isPaused) "RUNNING" else if (TimerState.isPaused) "PAUSED" else "READY",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (TimerState.isRunning && !TimerState.isPaused) Primary else Outline,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task Name Input
        OutlinedTextField(
            value = TimerState.currentTaskName,
            onValueChange = { TimerState.currentTaskName = it },
            label = { Text("What are you working on?", fontFamily = ManropeFontFamily) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary,
                unfocusedBorderColor = Outline.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = !TimerState.isRunning // Lock name while running to avoid edit confusion
        )

        // Project Dropdown Selector
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = TimerState.currentProject,
                onValueChange = {},
                readOnly = true,
                label = { Text("Project", fontFamily = ManropeFontFamily) },
                trailingIcon = {
                    IconButton(onClick = { if (!TimerState.isRunning) isProjectDropdownExpanded = true }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Primary)
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable { if (!TimerState.isRunning) isProjectDropdownExpanded = true },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary,
                    unfocusedBorderColor = Outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !TimerState.isRunning
            )

            DropdownMenu(
                expanded = isProjectDropdownExpanded,
                onDismissRequest = { isProjectDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.85f).background(WarmBackground)
            ) {
                projectList.forEach { proj ->
                    DropdownMenuItem(
                        text = { Text(proj, fontFamily = ManropeFontFamily) },
                        onClick = {
                            TimerState.currentProject = proj
                            isProjectDropdownExpanded = false
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    projectToDelete = proj
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add New Project",
                                fontFamily = ManropeFontFamily,
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    onClick = {
                        isProjectDropdownExpanded = false
                        showAddProjectDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Timer Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!TimerState.isRunning) {
                // START Button
                Button(
                    onClick = {
                        TimerState.isRunning = true
                        TimerState.isPaused = false
                        TimerState.secondsElapsed = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = OnPrimaryFixed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Timer", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnPrimaryFixed)
                }
            } else {
                // PAUSE / RESUME Button
                Button(
                    onClick = { TimerState.isPaused = !TimerState.isPaused },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryContainer),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Icon(
                        imageVector = if (TimerState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        tint = OnSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (TimerState.isPaused) "Resume" else "Pause",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = OnSecondaryContainer
                    )
                }

                // STOP & SAVE Button
                Button(
                    onClick = {
                        val durationMins = (TimerState.secondsElapsed / 60).coerceAtLeast(1).toInt()
                        val taskName = TimerState.currentTaskName.trim().ifEmpty { "Active Session" }
                        val projectSelected = TimerState.currentProject

                        // Infer category
                        val category = when {
                            taskName.contains("mail", true) || taskName.contains("admin", true) || taskName.contains("sync", true) -> "Admin"
                            taskName.contains("break", true) || taskName.contains("coffee", true) || taskName.contains("relax", true) -> "Leisure"
                            taskName.contains("health", true) || taskName.contains("run", true) || taskName.contains("gym", true) -> "Health"
                            else -> "Focus"
                        }

                        val currentMillis = System.currentTimeMillis()
                        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        // Subtract elapsed seconds to estimate correct start time
                        val startCal = Calendar.getInstance().apply {
                            timeInMillis = currentMillis - (TimerState.secondsElapsed * 1000)
                        }
                        val startTime = sdf.format(startCal.time)

                        coroutineScope.launch {
                            val success = SupabaseManager.addActivity(
                                ActivityRecordDto(
                                    user_id = userId,
                                    activity_name = taskName,
                                    project = projectSelected,
                                    category = category,
                                    duration = durationMins,
                                    notes = "Logged via Timer",
                                    date_millis = currentMillis,
                                    start_time = startTime
                                )
                            )

                            if (success) {
                                Toast.makeText(context, "Session saved: ${durationMins}m", Toast.LENGTH_SHORT).show()
                                // Reset state
                                TimerState.isRunning = false
                                TimerState.isPaused = false
                                TimerState.secondsElapsed = 0
                                TimerState.currentTaskName = ""
                                TimerState.currentProject = ""
                                onSaved()
                            } else {
                                Toast.makeText(context, "Failed to save session", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = OnPrimaryFixed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop & Save", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, color = OnPrimaryFixed)
                }
            }
        }
    }
}

// REPORTS TAB CONTENT
@Composable
fun ReportsTabContent(userId: String) {
    var period by remember { mutableStateOf("Weekly") }
    var allActivities by remember { mutableStateOf<List<ActivityRecord>>(emptyList()) }
    LaunchedEffect(period) {
        allActivities = SupabaseManager.getAllActivities(userId)
    }

    val filteredActivities = remember(period, allActivities) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        when (period) {
            "Daily" -> allActivities.filter { isSameDay(it.dateMillis, now) }
            "Weekly" -> {
                val weekAgo = now - (7 * 24 * 60 * 60 * 1000L)
                allActivities.filter { it.dateMillis in weekAgo..now }
            }
            "Monthly" -> {
                val monthAgo = now - (30 * 24 * 60 * 60 * 1000L)
                allActivities.filter { it.dateMillis in monthAgo..now }
            }
            else -> allActivities
        }
    }

    val totalMinutes = filteredActivities.sumOf { it.duration }

    val categoryData = remember(filteredActivities, totalMinutes) {
        filteredActivities.groupBy { it.category }
            .map { (category, activities) ->
                val catMinutes = activities.sumOf { it.duration }
                val percentage = if (totalMinutes > 0) (catMinutes * 100 / totalMinutes) else 0
                val icon = when(category) {
                    "Focus" -> Icons.Default.Psychology
                    "Admin" -> Icons.Default.Email
                    "Leisure" -> Icons.Default.LocalCafe
                    "Health" -> Icons.Default.DirectionsRun
                    else -> Icons.Default.Timer
                }
                CategoryReport(category, "${catMinutes / 60}h ${catMinutes % 60}m", percentage, icon)
            }.sortedByDescending { it.percentage }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Period Toggle Button Row
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(SurfaceContainer),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Daily", "Weekly", "Monthly").forEach { opt ->
                val isSelected = period == opt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) PrimaryContainer else Color.Transparent)
                        .clickable { period = opt },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isSelected) OnPrimaryFixed else OnSurfaceVariant
                    )
                }
            }
        }

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TOTAL TIME LOGGED",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Outline,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${totalMinutes / 60}h ${totalMinutes % 60}m",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    color = OnSurface
                )
            }
        }

        // Category breakdown progress list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (categoryData.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data to report.", fontFamily = ManropeFontFamily, color = Outline)
                    }
                }
            } else {
                items(categoryData) { cat ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = cat.icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                Text(cat.name, fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            Text("${cat.percentage}% (${cat.time})", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)).background(SurfaceContainer)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(cat.percentage / 100f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(PrimaryContainer)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class CategoryReport(val name: String, val time: String, val percentage: Int, val icon: ImageVector)

// SETTINGS TAB CONTENT
@Composable
fun SettingsTabContent(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPref = remember { context.getSharedPreferences("UserSession", Context.MODE_PRIVATE) }

    val username = sharedPref.getString("username", "User") ?: "User"
    val email = sharedPref.getString("email", "user@example.com") ?: "user@example.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.take(1).uppercase(),
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = OnPrimaryFixed
                    )
                }

                Column {
                    Text(
                        text = username,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = OnSurface
                    )
                    Text(
                        text = email,
                        fontFamily = ManropeFontFamily,
                        color = Outline,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Settings items
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SettingsItem(
                icon = Icons.Outlined.Notifications,
                title = "Notification Settings",
                onClick = { navController.navigate("notification_settings") }
            )

            // Dark Mode Item with Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.DarkMode, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                        Text("Dark Mode", fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface)
                    }
                    Switch(
                        checked = ThemeState.isDarkMode,
                        onCheckedChange = { isChecked ->
                            ThemeState.isDarkMode = isChecked
                            sharedPref.edit().putBoolean("isDarkMode", isChecked).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Primary,
                            checkedTrackColor = PrimaryContainer
                        )
                    )
                }
            }

            SettingsItem(
                icon = Icons.Outlined.PrivacyTip,
                title = "Privacy Policy",
                onClick = { navController.navigate("privacy_policy") }
            )
            SettingsItem(icon = Icons.Outlined.Info, title = "App Version 2.5.0")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout Button
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        SupabaseManager.client.auth.signOut()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    sharedPref.edit().apply {
                        putBoolean("isLoggedIn", false)
                        putString("userId", "")
                        putString("username", "")
                        putString("email", "")
                        apply()
                    }
                    // Reset active timer if running
                    TimerState.isRunning = false
                    TimerState.secondsElapsed = 0
                    TimerState.currentTaskName = ""
                    TimerState.currentProject = ""

                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer),
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = OnErrorContainer)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, color = OnErrorContainer, fontSize = 16.sp)
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                Text(title, fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Outline)
        }
    }
}

// FLOATING ACTION BUTTON
@Composable
fun AddFab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                spotColor = Color(0x66FFD541),
                ambientColor = Color(0x33FFD541),
            )
            .clip(CircleShape)
            .background(PrimaryContainer)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Task",
            tint = OnPrimaryFixed,
            modifier = Modifier.size(28.dp)
        )
    }
}

// BOTTOM NAVIGATION
@Composable
fun MomentumBottomNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navItems = listOf(
        NavItem("Home", Icons.Outlined.Home, Icons.Filled.Home),
        NavItem("History", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
        NavItem("Start", Icons.Filled.PlayArrow, Icons.Filled.PlayArrow),
        NavItem("Reports", Icons.Outlined.BarChart, Icons.Filled.BarChart),
        NavItem("Setting", Icons.Outlined.Settings, Icons.Filled.Settings),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 0.dp,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            )
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(WarmBackground.copy(alpha = 0.95f))
            .navigationBarsPadding()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp,
            )
    ) {
        HorizontalDivider(
            color = CardBorder,
            thickness = 1.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            navItems.forEachIndexed { index, item ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (index == 2) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.offset(y = (-16).dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = CircleShape,
                                        spotColor = Color(0x33FFD541),
                                    )
                                    .clip(CircleShape)
                                    .background(PrimaryContainer)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) { onItemSelected(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Start",
                                    tint = OnPrimaryFixed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "START",
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 8.sp,
                                letterSpacing = 0.8.sp,
                                color = if (selectedIndex == index) OnSurface else OnSurfaceVariant.copy(alpha = 0.5f),
                            )
                        }
                    } else {
                        val isSelected = selectedIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) SurfaceContainerLowest.copy(alpha = 0.5f)
                                    else Color.Transparent
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { onItemSelected(index) }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) OnSurface else OnSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.label.uppercase(),
                                    fontFamily = ManropeFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 8.sp,
                                    letterSpacing = 0.8.sp,
                                    color = if (isSelected) OnSurface else OnSurfaceVariant.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// HELPER FUNCTIONS
private fun formatElapsedTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}