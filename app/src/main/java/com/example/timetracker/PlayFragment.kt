package com.example.timetracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.timetracker.databinding.FragmentPlayBinding

class PlayFragment : Fragment() {

    private var _binding: FragmentPlayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nanti logika start/pause/stop timer kamu taruh di sini ya
        // Contoh:
        // binding.btnStart.setOnClickListener { ... }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}