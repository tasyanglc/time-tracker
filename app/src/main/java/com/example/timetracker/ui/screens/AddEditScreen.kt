package com.example.timetracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.timetracker.DatabaseHelper
import com.example.timetracker.ui.theme.ManropeFontFamily
import com.example.timetracker.ui.theme.OnPrimaryFixed
import com.example.timetracker.ui.theme.OnSurface
import com.example.timetracker.ui.theme.OnSurfaceVariant
import com.example.timetracker.ui.theme.Outline
import com.example.timetracker.ui.theme.Primary
import com.example.timetracker.ui.theme.PrimaryContainer
import com.example.timetracker.ui.theme.WarmBackground
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(navController: NavController) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }

    var activityName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var project by remember { mutableStateOf("Work") }
    var durationText by remember { mutableStateOf("45") }
    
    var projectDropdownExpanded by remember { mutableStateOf(false) }
    val projectList = listOf("Work", "Education", "Personal", "Health", "Hobby", "Finance", "Social")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Activity",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmBackground
                )
            )
        },
        containerColor = WarmBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Activity Name Field
            OutlinedTextField(
                value = activityName,
                onValueChange = { activityName = it },
                label = { Text("Activity Name", fontFamily = ManropeFontFamily) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary,
                    unfocusedBorderColor = Outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Project Field (Dropdown Selector)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = project,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Project", fontFamily = ManropeFontFamily) },
                    trailingIcon = {
                        IconButton(onClick = { projectDropdownExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Primary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { projectDropdownExpanded = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary,
                        unfocusedBorderColor = Outline.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                
                DropdownMenu(
                    expanded = projectDropdownExpanded,
                    onDismissRequest = { projectDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f).background(WarmBackground)
                ) {
                    projectList.forEach { proj ->
                        DropdownMenuItem(
                            text = { Text(proj, fontFamily = ManropeFontFamily) },
                            onClick = {
                                project = proj
                                projectDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Duration Field
            OutlinedTextField(
                value = durationText,
                onValueChange = { durationText = it },
                label = { Text("Duration (minutes or HH:MM)", fontFamily = ManropeFontFamily) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Primary
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary,
                    unfocusedBorderColor = Outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Notes Field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)", fontFamily = ManropeFontFamily) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary,
                    unfocusedBorderColor = Outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    val trimName = activityName.trim()
                    val trimNotes = notes.trim()
                    val trimDuration = durationText.trim()

                    if (trimName.isEmpty()) {
                        Toast.makeText(context, "Please enter an activity name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    var durationMinutes = 45 // default fallback
                    if (trimDuration.isNotEmpty()) {
                        try {
                            if (trimDuration.contains(":")) {
                                val parts = trimDuration.split(":")
                                val hours = parts[0].trim().toIntOrNull() ?: 0
                                val mins = parts[1].trim().toIntOrNull() ?: 0
                                durationMinutes = (hours * 60) + mins
                            } else {
                                durationMinutes = trimDuration.toIntOrNull() ?: 45
                            }
                        } catch (e: Exception) {
                            durationMinutes = 45
                        }
                    }

                    // Infer category based on name keywords just like original AddEditActivity
                    val category = when {
                        trimName.contains("mail", true) || trimName.contains("admin", true) || trimName.contains("sync", true) -> "Admin"
                        trimName.contains("break", true) || trimName.contains("coffee", true) || trimName.contains("relax", true) -> "Leisure"
                        trimName.contains("health", true) || trimName.contains("run", true) || trimName.contains("gym", true) -> "Health"
                        else -> "Focus"
                    }

                    val currentMillis = System.currentTimeMillis()
                    val sdf = SimpleDateFormat("hh:mm AM", Locale.getDefault())
                    val startTime = sdf.format(Calendar.getInstance().time)

                    val result = dbHelper.addActivity(
                        name = trimName,
                        project = project,
                        category = category,
                        duration = durationMinutes,
                        notes = trimNotes,
                        date = currentMillis,
                        time = startTime
                    )

                    if (result != -1L) {
                        Toast.makeText(context, "Activity saved successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Failed to save activity", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Save Activity",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = OnPrimaryFixed
                )
            }
        }
    }
}
