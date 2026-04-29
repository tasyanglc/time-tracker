package com.example.timetracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timetracker.ui.theme.*


// DATA CLASSES

data class ActivityItem(
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


// HOME SCREEN

@Composable
fun HomeScreen() {
    val scrollState = rememberScrollState()
    var selectedNavItem by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
    ) {
        // ── Konten berubah sesuai nav ──
        when (selectedNavItem) {
            0 -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = 56.dp, bottom = 130.dp)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                TodaySummaryCard()
                ActiveTimerWidget()
                RecentActivitiesSection()
                Spacer(Modifier.height(8.dp))
            }
            1 -> PlaceholderScreen("History")
            2 -> PlaceholderScreen("Start")
            3 -> PlaceholderScreen("Reports")
            4 -> PlaceholderScreen("Settings")
        }

        // ── Top App Bar (fixed) ──
        MomentumTopBar(
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // ── FAB (fixed, above bottom nav) ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 120.dp, end = 24.dp)
        ) {
            AddFab()
        }

        // ── Bottom Nav (fixed) ──
        MomentumBottomNav(
            selectedIndex = selectedNavItem,
            onItemSelected = { selectedNavItem = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = OnSurface
        )
    }
}

// TOP APP BAR

@Composable
fun MomentumTopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(WarmBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Hamburger + App Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = OnSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Momentum",
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
                text = "J",
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
fun TodaySummaryCard() {
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
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = CardBorder.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
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
                        text = "6h 45m",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        lineHeight = 40.sp,
                        letterSpacing = (-0.64).sp,
                        color = OnSurface,
                    )
                }

                // Icon Badge
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

            // Progress Section
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
                    text = "84%",
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
                        .fillMaxWidth(0.84f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(PrimaryContainer)
                )
            }
        }
    }
}

// ACTIVE TIMER WIDGET

@Composable
fun ActiveTimerWidget() {
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
    ) {
        // Decorative circle (top-right background element)
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
            // Task Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Current Task: Coding",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = OnPrimaryFixed,
                )
            }

            // Timer Display
            Text(
                text = "00:42:15",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                lineHeight = 52.sp,
                letterSpacing = (-1.5).sp,
                color = OnPrimaryFixed,
                textAlign = TextAlign.Center,
            )

            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause Button (small)
                TimerControlButton(
                    size = 48,
                    bgColor = Color.White.copy(alpha = 0.3f),
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = OnPrimaryFixed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Stop Button (large, primary action)
                TimerControlButton(
                    size = 64,
                    bgColor = OnPrimaryFixed,
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Refresh Button (small)
                TimerControlButton(
                    size = 48,
                    bgColor = Color.White.copy(alpha = 0.3f),
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart",
                        tint = OnPrimaryFixed,
                        modifier = Modifier.size(24.dp)
                    )
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
fun RecentActivitiesSection() {
    val activities = listOf(
        ActivityItem(
            icon = Icons.Outlined.Palette,
            iconBgColor = TertiaryFixed,
            iconTintColor = OnTertiaryFixed,
            title = "Design Sync",
            timeRange = "10:00 AM - 11:30 AM",
            duration = "1h 30m",
        ),
        ActivityItem(
            icon = Icons.Outlined.Email,
            iconBgColor = SecondaryContainer,
            iconTintColor = OnSecondaryContainer,
            title = "Email Management",
            timeRange = "08:45 AM - 09:15 AM",
            duration = "30m",
        ),
        ActivityItem(
            icon = Icons.Outlined.LocalCafe,
            iconBgColor = CoffeeIconBg,
            iconTintColor = CoffeeIconColor,
            title = "Quick Break",
            timeRange = "08:30 AM - 08:45 AM",
            duration = "15m",
        ),
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Section Header
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
            TextButton(onClick = {}) {
                Text(
                    text = "View All",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Primary,
                )
            }
        }

        // Activity Items
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            activities.forEach { activity ->
                ActivityCard(activity = activity)
            }
        }
    }
}

@Composable
fun ActivityCard(activity: ActivityItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                // Icon Container
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

                // Title & Time Range
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
                        letterSpacing = 0.6.sp,
                        color = Outline,
                    )
                }
            }

            // Duration
            Text(
                text = activity.duration,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = OnSurfaceVariant,
            )
        }
    }
}

// FAB (Floating Action Button)

@Composable
fun AddFab() {
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
                onClick = {}
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

// BOTTOM NAVIGATION BAR

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
        NavItem("Settings", Icons.Outlined.Settings, Icons.Filled.Settings),
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
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp,
            )
    ) {
        // Top border line
        Divider(
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
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom,
        ) {
            navItems.forEachIndexed { index, item ->
                if (index == 2) {
                    // Center START button (elevated)
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
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = if (selectedIndex == index) OnSurface else OnSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                } else {
                    // Regular nav item
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
                                fontSize = 10.sp,
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

// PREVIEW

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    com.example.timetracker.ui.theme.TimetrackerTheme {
        HomeScreen()
    }
}