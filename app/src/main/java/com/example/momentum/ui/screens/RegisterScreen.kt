package com.example.momentum.ui.screens

import android.util.Patterns
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.example.momentum.SupabaseManager
import com.example.momentum.ui.theme.ManropeFontFamily
import com.example.momentum.ui.theme.OnPrimaryFixed
import com.example.momentum.ui.theme.OnSurface
import com.example.momentum.ui.theme.OnSurfaceVariant
import com.example.momentum.ui.theme.Outline
import com.example.momentum.ui.theme.Primary
import com.example.momentum.ui.theme.PrimaryContainer
import com.example.momentum.ui.theme.WarmBackground

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Create Account",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                color = OnSurface,
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = "Sign up to start tracking your time.",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = OnSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )

            // Username Input
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", fontFamily = ManropeFontFamily) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
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

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontFamily = ManropeFontFamily) },
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

            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", fontFamily = ManropeFontFamily) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Primary
                    )
                },
                trailingIcon = {
                    val image = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Primary)
                    }
                },
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Create Account Button
            Button(
                onClick = {
                    val trimUser = username.trim()
                    val trimEmail = email.trim()
                    val trimPass = password.trim()
                    val trimConf = confirmPassword.trim()

                    if (trimUser.isEmpty() || trimEmail.isEmpty() || trimPass.isEmpty() || trimConf.isEmpty()) {
                        Toast.makeText(context, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(trimEmail).matches()) {
                        Toast.makeText(context, "Email tidak valid", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (trimPass.length < 8) {
                        Toast.makeText(context, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (trimPass != trimConf) {
                        Toast.makeText(context, "Password dan konfirmasi harus sama", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        try {
                            SupabaseManager.client.auth.signUpWith(Email) {
                                this.email = trimEmail
                                this.password = trimPass
                                this.data = buildJsonObject {
                                    put("username", trimUser)
                                }
                            }
                            dbHelper.addUser(trimUser, trimEmail, trimPass)
                            Toast.makeText(context, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show()
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Registrasi gagal: ${e.localizedMessage ?: "Terjadi kesalahan"}", Toast.LENGTH_LONG).show()
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
                    text = "Sign Up",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = OnPrimaryFixed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Link to Login
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have an account? ",
                    fontFamily = ManropeFontFamily,
                    color = OnSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign In",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
