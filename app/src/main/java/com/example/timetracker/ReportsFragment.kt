package com.example.timetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timetracker.databinding.FragmentReportsBinding
import com.example.timetracker.databinding.ItemCategoryBinding
import java.util.Calendar

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        dbHelper = DatabaseHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadReportData("Weekly") // Default

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnDaily -> loadReportData("Daily")
                    R.id.btnWeekly -> loadReportData("Weekly")
                    R.id.btnMonthly -> loadReportData("Monthly")
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvCategories.layoutManager = LinearLayoutManager(context)
    }

    private fun loadReportData(period: String) {
        val allActivities = dbHelper.getAllActivities()
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        val filteredActivities = when (period) {
            "Daily" -> {
                allActivities.filter { isSameDay(it.dateMillis, now) }
            }
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

        val totalMinutes = filteredActivities.sumOf { it.duration }
        
        // Group by category
        val categoryData = filteredActivities.groupBy { it.category }
            .map { (category, activities) ->
                val catMinutes = activities.sumOf { it.duration }
                val percentage = if (totalMinutes > 0) (catMinutes * 100 / totalMinutes) else 0
                val icon = when(category) {
                    "Focus" -> R.drawable.ic_moon
                    "Admin" -> R.drawable.ic_menu
                    "Leisure" -> R.drawable.ic_moon
                    "Health" -> R.drawable.ic_moon
                    else -> R.drawable.ic_timer
                }
                CategoryReport(category, "${catMinutes / 60}h ${catMinutes % 60}m", percentage, icon)
            }.sortedByDescending { it.percentage }

        binding.rvCategories.adapter = CategoriesAdapter(categoryData)
        
        // Update Chart & Total
        binding.tvTotalTimeReports.text = "${totalMinutes / 60}.${(totalMinutes % 60) / 6}h"
        
        // Update ProgressBars for chart
        binding.pbFocus.progress = categoryData.find { it.name == "Focus" }?.percentage ?: 0
        binding.pbAdmin.progress = categoryData.find { it.name == "Admin" }?.percentage ?: 0
        binding.pbLeisure.progress = categoryData.find { it.name == "Leisure" }?.percentage ?: 0
        binding.pbHealth.progress = categoryData.find { it.name == "Health" }?.percentage ?: 0
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class CategoryReport(val name: String, val time: String, val percentage: Int, val iconRes: Int)

    inner class CategoriesAdapter(private val categories: List<CategoryReport>) :
        RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category = categories[position]
            holder.binding.tvCategory.text = category.name
            holder.binding.tvTime.text = category.time
            holder.binding.tvPercentage.text = "${category.percentage}%"
            holder.binding.pbCategory.progress = category.percentage
            holder.binding.ivIcon.setImageResource(category.iconRes)
        }

        override fun getItemCount() = categories.size
    }
}