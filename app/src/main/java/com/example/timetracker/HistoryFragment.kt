package com.example.timetracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timetracker.databinding.FragmentHistoryBinding
import com.example.timetracker.databinding.ItemActivityBinding
import java.util.Calendar

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        dbHelper = DatabaseHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCalendarClicks()
        loadDataForSelectedDate()
    }

    private fun setupRecyclerView() {
        binding.rvActivities.layoutManager = LinearLayoutManager(context)
    }

    private fun setupCalendarClicks() {
        for (i in 0 until binding.calendarGrid.childCount) {
            val child = binding.calendarGrid.getChildAt(i)
            if (child is TextView && child.text.isNotEmpty() && child.text.toString().toIntOrNull() != null) {
                child.setOnClickListener {
                    val day = child.text.toString().toInt()
                    selectedDate.set(Calendar.DAY_OF_MONTH, day)
                    loadDataForSelectedDate()
                    
                    // Highlight selected day (simplified)
                    clearCalendarSelection()
                    child.setBackgroundResource(R.drawable.bg_circle_yellow)
                }
            }
        }
    }

    private fun clearCalendarSelection() {
        for (i in 0 until binding.calendarGrid.childCount) {
            val child = binding.calendarGrid.getChildAt(i)
            if (child is TextView && child.text.isNotEmpty() && child.text.toString().toIntOrNull() != null) {
                child.background = null
            }
        }
    }

    private fun loadDataForSelectedDate() {
        val records = dbHelper.getActivitiesByDate(selectedDate.timeInMillis)
        binding.rvActivities.adapter = ActivitiesAdapter(records)
        
        val totalMinutes = records.sumOf { it.duration }
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        binding.tvTotalTimeLogged.text = "${hours}h ${mins}m"
        
        val monthSdf = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
        binding.tvMonth.text = monthSdf.format(selectedDate.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class ActivitiesAdapter(private val activities: List<ActivityRecord>) :
        RecyclerView.Adapter<ActivitiesAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemActivityBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val activity = activities[position]
            holder.binding.tvTitle.text = activity.name
            holder.binding.tvProject.text = activity.project
            holder.binding.tvDuration.text = "${activity.duration / 60}h ${activity.duration % 60}m"
            holder.binding.tvTimeRange.text = activity.startTime

            holder.binding.root.setOnClickListener {
                val intent = Intent(context, AddEditActivity::class.java)
                // Pass data if editing is supported
                startActivity(intent)
            }
        }

        override fun getItemCount() = activities.size
    }
}