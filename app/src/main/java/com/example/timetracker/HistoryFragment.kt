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

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCalendarClicks()
    }

    private fun setupCalendarClicks() {
        for (i in 0 until binding.calendarGrid.childCount) {
            val child = binding.calendarGrid.getChildAt(i)
            if (child is TextView) {
                child.setOnClickListener {
                    Toast.makeText(context, "Detail for day: ${child.text}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvActivities.layoutManager = LinearLayoutManager(context)
        binding.rvActivities.adapter = ActivitiesAdapter(getMockActivities())
    }

    private fun getMockActivities(): List<TimeActivity> {
        return listOf(
            TimeActivity("UI Design Refinement", "Project Momentum", "2h 15m", "09:30 AM"),
            TimeActivity("Stakeholder Meeting", "Sync Session", "1h 00m", "11:45 AM"),
            TimeActivity("Code Review", "Project Momentum", "45m", "02:00 PM"),
            TimeActivity("Documentation", "Internal", "1h 30m", "03:30 PM")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class TimeActivity(val title: String, val project: String, val duration: String, val time: String)

    inner class ActivitiesAdapter(private val activities: List<TimeActivity>) :
        RecyclerView.Adapter<ActivitiesAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemActivityBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val activity = activities[position]
            holder.binding.tvTitle.text = activity.title
            holder.binding.tvProject.text = activity.project
            holder.binding.tvDuration.text = activity.duration
            holder.binding.tvTimeRange.text = activity.time

            holder.binding.root.setOnClickListener {
                val intent = Intent(context, AddEditActivity::class.java)
                startActivity(intent)
            }
        }

        override fun getItemCount() = activities.size
    }
}