package com.example.momentum.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.momentum.DatabaseHelper
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import com.example.momentum.SupabaseManager
import kotlinx.serialization.json.jsonPrimitive
import com.example.momentum.ui.theme.ManropeFontFamily
import com.example.momentum.ui.theme.OnPrimaryFixed
import com.example.momentum.ui.theme.OnSurface
import com.example.momentum.ui.theme.OnSurfaceVariant
import com.example.momentum.ui.theme.Outline
import com.example.momentum.ui.theme.Primary
import com.example.momentum.ui.theme.PrimaryContainer
import com.example.momentum.ui.theme.WarmBackground

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    var identity by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome Back",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                color = OnSurface,
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = "Sign in to continue tracking your momentum.",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = OnSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )

            // Identity Input (Username / Email)
            OutlinedTextField(
                value = identity,
                onValueChange = { identity = it },
                label = { Text("Username or Email", fontFamily = ManropeFontFamily) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Primary
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary,
                    unfocusedBorderColor = Outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontFamily = ManropeFontFamily) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Primary
                    )
                },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Primary)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary,
                    unfocusedBorderColor = Outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sign In Button
            Button(
                onClick = {
                    val trimIdentity = identity.trim()
                    val trimPassword = password.trim()

                    if (trimIdentity.isEmpty() || trimPassword.isEmpty()) {
                        Toast.makeText(context, "Username/Email dan Password wajib diisi", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        try {
                            var emailToSignIn = trimIdentity
                            if (!trimIdentity.contains("@")) {
                                val details = dbHelper.getUserDetails(trimIdentity)
                                if (details != null) {
                                    emailToSignIn = details["email"] ?: trimIdentity
                                }
                            }

                            SupabaseManager.client.auth.signInWith(Email) {
                                this.email = emailToSignIn
                                this.password = trimPassword
                            }
                            val user = SupabaseManager.client.auth.currentUserOrNull()
                            if (user != null) {
                                val uName = user.userMetadata?.get("username")?.jsonPrimitive?.content
                                    ?: user.email?.substringBefore("@")
                                    ?: "User"

                                // Auto-sync locally
                                if (!dbHelper.isUsernameTaken(uName)) {
                                    dbHelper.addUser(uName, user.email ?: "", trimPassword)
                                }

                                val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                                sharedPref.edit().apply {
                                    putBoolean("isLoggedIn", true)
                                    putString("username", uName)
                                    putString("email", user.email ?: "")
                                    putString("userId", user.id)
                                    apply()
                                }
                                Toast.makeText(context, "Login berhasil", Toast.LENGTH_SHORT).show()
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Gagal mengambil data user", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Login gagal: ${e.localizedMessage ?: "Email atau Password salah"}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = OnPrimaryFixed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Link to Register
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontFamily = ManropeFontFamily,
                    color = OnSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign Up",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("register")
                    }
                )
            }
        }
    }
}
