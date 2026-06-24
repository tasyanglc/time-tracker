package com.example.timetracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timetracker.databinding.ActivityAddEditBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            val activityName = binding.etActivityName.text.toString().trim()
            val notes = binding.etNotes.text.toString().trim()
            val project = binding.tvProjectDropdown.text.toString().trim()
            val durationText = binding.etDuration.text.toString().trim()

            if (activityName.isEmpty()) {
                Toast.makeText(this, "Please enter an activity name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Parse duration (HH:MM or minutes)
            var durationMinutes = 45 // default fallback
            if (durationText.isNotEmpty()) {
                try {
                    if (durationText.contains(":")) {
                        val parts = durationText.split(":")
                        val hours = parts[0].trim().toIntOrNull() ?: 0
                        val mins = parts[1].trim().toIntOrNull() ?: 0
                        durationMinutes = (hours * 60) + mins
                    } else {
                        durationMinutes = durationText.toIntOrNull() ?: 45
                    }
                } catch (e: Exception) {
                    durationMinutes = 45
                }
            }

            // Infer category based on name keywords just for mock matching/reports
            val category = when {
                activityName.contains("mail", true) || activityName.contains("admin", true) || activityName.contains("sync", true) -> "Admin"
                activityName.contains("break", true) || activityName.contains("coffee", true) || activityName.contains("relax", true) -> "Leisure"
                activityName.contains("health", true) || activityName.contains("run", true) || activityName.contains("gym", true) -> "Health"
                else -> "Focus"
            }

            val currentMillis = System.currentTimeMillis()
            val sdf = SimpleDateFormat("hh:mm AM", Locale.getDefault())
            val startTime = sdf.format(Calendar.getInstance().time)

            val result = dbHelper.addActivity(
                name = activityName,
                project = project,
                category = category,
                duration = durationMinutes,
                notes = notes,
                date = currentMillis,
                time = startTime
            )

            if (result != -1L) {
                Toast.makeText(this, "Activity saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save activity", Toast.LENGTH_SHORT).show()
            }
        }
    }
}