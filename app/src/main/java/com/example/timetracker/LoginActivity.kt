package com.example.timetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        val etIdentity = findViewById<EditText>(R.id.login_et_identity)
        val etPassword = findViewById<EditText>(R.id.login_et_password)
        val btnSignIn = findViewById<AppCompatButton>(R.id.login_btn_signin)
        val btnToRegister = findViewById<LinearLayout>(R.id.btn_to_register)

        btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        btnSignIn.setOnClickListener {
            val identity = etIdentity.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (identity.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username/Email dan Password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.checkUser(identity, password)) {
                val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putBoolean("isLoggedIn", true)
                    putString("username", identity)
                    apply()
                }

                Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Username/Email atau Password salah", Toast.LENGTH_SHORT).show()
            }
        }
    }
}