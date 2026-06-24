package com.example.timetracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        val etUsername = findViewById<EditText>(R.id.reg_et_name)
        val etEmail = findViewById<EditText>(R.id.reg_et_email)
        val etPassword = findViewById<EditText>(R.id.reg_et_password)
        val etConfirm = findViewById<EditText>(R.id.reg_et_confirm)
        val btnCreate = findViewById<AppCompatButton>(R.id.reg_btn_create)
        val btnToLogin = findViewById<LinearLayout>(R.id.btn_to_login)

        btnToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnCreate.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Password dan konfirmasi harus sama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.isUsernameTaken(username)) {
                Toast.makeText(this, "Username sudah digunakan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.isEmailTaken(email)) {
                Toast.makeText(this, "Email sudah digunakan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = dbHelper.addUser(username, email, password)
            if (result > -1) {
                Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Registrasi gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}