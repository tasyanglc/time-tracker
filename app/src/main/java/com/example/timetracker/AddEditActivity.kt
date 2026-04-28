package com.example.timetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.timetracker.databinding.ActivityAddEditBinding

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menerapkan ViewBinding pada Activity
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent logic: Menutup halaman saat tombol X diklik
        binding.btnClose.setOnClickListener {
            finish() // Mengakhiri Activity ini dan kembali ke layar sebelumnya
        }

        binding.btnSave.setOnClickListener {
            // Ambil data dari EditText
            val activityName = binding.etActivityName.text.toString()
            val notes = binding.etNotes.text.toString()

            // Simpan data (Logika Database/API nanti di sini)

            // Tutup halaman setelah save
            finish()
        }
    }
}