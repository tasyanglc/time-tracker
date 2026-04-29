package com.example.timetracker // Pastikan ini sesuai package name kamu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Inisialisasi komponen dari XML
        val etEmail = findViewById<EditText>(R.id.log_et_email)
        val etPassword = findViewById<EditText>(R.id.log_et_password)
        val btnLogin = findViewById<Button>(R.id.log_btn_login)
        val btnToRegister = findViewById<Button>(R.id.log_btn_to_register)
        val tvForgotPassword = findViewById<TextView>(R.id.tv_forgot_password)

        // 2. Logika klik tombol Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            // Validasi simpel: email & password tidak boleh kosong
            if (email.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                // Pindah ke halaman Home (MainActivity)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Agar tidak bisa 'back' ke halaman login lagi
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Logika klik tombol Register (Pindah ke RegisterActivity)
        btnToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 4. Logika klik Forgot Password
        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked! Coming soon.", Toast.LENGTH_SHORT).show()


        }
    }
}