package com.example.timetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timetracker.databinding.FragmentReportsBinding
import com.example.timetracker.databinding.ItemCategoryBinding

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.toggleGroup.addOnButtonCheckedListener { _, _, isChecked ->
            if (isChecked) {
                // Logic to filter data based on Daily, Weekly, Monthly can be added here
            }
        }
        
        binding.btnExport.setOnClickListener {
            // Optional export feature
        }
    }

    private fun setupRecyclerView() {
        binding.rvCategories.layoutManager = LinearLayoutManager(context)
        binding.rvCategories.adapter = CategoriesAdapter(getMockCategories())
    }

    private fun getMockCategories(): List<CategoryReport> {
        return listOf(
            CategoryReport("Deep Focus", "14h 25m", 45, R.drawable.ic_moon),
            CategoryReport("Admin & Email", "6h 15m", 25, R.drawable.ic_menu),
            CategoryReport("Health & Wellness", "4h 45m", 18, R.drawable.ic_moon)
        )
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

            holder.binding.root.setOnClickListener {
                Toast.makeText(context, "Category Detail: ${category.name}", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount() = categories.size
    }
}