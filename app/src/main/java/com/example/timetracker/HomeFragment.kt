package com.example.timetracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timetracker.databinding.FragmentHomeBinding
import com.example.timetracker.databinding.ItemActivityBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        binding.rvRecentActivities.layoutManager = LinearLayoutManager(context)
        binding.rvRecentActivities.adapter = ActivitiesAdapter(getMockRecentActivities())
    }

    private fun setupClickListeners() {
        binding.fabAddActivity.setOnClickListener {
            val intent = Intent(requireContext(), AddEditActivity::class.java)
            startActivity(intent)
        }

        binding.cardActiveTimer.setOnClickListener {
            // Logic for Timer Detail - for now just Toast or navigate to a detail if it exists
        }

        binding.tvViewAll.setOnClickListener {
            // Navigate to History/Calendar fragment
            (activity as? MainActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_history
        }
    }

    private fun getMockRecentActivities(): List<TimeActivity> {
        return listOf(
            TimeActivity("Design Sync", "Project Momentum", "1h 30m", "10:00 AM"),
            TimeActivity("Email Management", "Admin", "30m", "08:45 AM"),
            TimeActivity("Quick Break", "Personal", "15m", "08:30 AM")
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