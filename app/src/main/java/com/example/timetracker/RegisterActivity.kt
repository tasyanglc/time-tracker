package com.example.timetracker
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class RegisterActivity : AppCompatActivity() {


   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_register)


       // variabel dengan ID di XML
       val btnCreateAccount = findViewById<Button>(R.id.reg_btn_create)
       val btnBackToLogin = findViewById<LinearLayout>(R.id.btn_to_login)


       val etName = findViewById<EditText>(R.id.reg_et_name)
       val etEmail = findViewById<EditText>(R.id.reg_et_email)
       val etPassword = findViewById<EditText>(R.id.reg_et_password)


       // Aksi tombol Create Account
       btnCreateAccount.setOnClickListener {
           val name = etName.text.toString()


           if (name.isEmpty()) {
               Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
           } else {
               // Langsung ke home setelah regist
               Toast.makeText(this, "Welcome, $name! Account Created.", Toast.LENGTH_LONG).show()


               val intent = Intent(this, MainActivity::class.java)
               startActivity(intent)
               finish() // Tutup halaman register
           }
       }


       // Aksi tulisan "Sign In"
       btnBackToLogin.setOnClickListener {
           val intent = Intent(this, LoginActivity::class.java)
           startActivity(intent)
           finish()
       }
   }
}
