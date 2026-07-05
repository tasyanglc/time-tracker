package com.example.momentum.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.momentum.DatabaseHelper
import com.example.momentum.SupabaseManager
import com.example.momentum.ui.theme.*
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dbHelper = remember { DatabaseHelper(context) }
    val sharedPref = remember { context.getSharedPreferences("UserSession", Context.MODE_PRIVATE) }

    val oldUsername = sharedPref.getString("username", "User") ?: "User"
    val oldEmail = sharedPref.getString("email", "") ?: ""
    val sessionProfileImage = sharedPref.getString("profile_image", "") ?: ""

    var username by remember { mutableStateOf(oldUsername) }
    var email by remember { mutableStateOf(oldEmail) }
    var password by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf(sessionProfileImage) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Load profile photo bitmap
    val profileBitmap = remember(profileImageUri) {
        if (profileImageUri.isNotEmpty()) {
            try {
                val uri = Uri.parse(profileImageUri)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val contentResolver = context.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            profileImageUri = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmBackground)
            )
        },
        containerColor = WarmBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Picture Circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profileBitmap != null) {
                    Image(
                        bitmap = profileBitmap,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = username.take(1).uppercase(),
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        color = OnPrimaryFixed
                    )
                }

                // Overlay Camera Icon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change photo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = "Ketuk untuk mengubah foto profil",
                fontFamily = ManropeFontFamily,
                fontSize = 12.sp,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Username input
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", fontFamily = ManropeFontFamily) },
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Primary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary,
                    unfocusedBorderColor = Outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Email input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontFamily = ManropeFontFamily) },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Primary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary,
                    unfocusedBorderColor = Outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Password input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password Baru (kosongkan jika tidak diubah)", fontFamily = ManropeFontFamily) },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Primary) },
                visualTransformation = PasswordVisualTransformation(),
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

            // Save changes button
            Button(
                onClick = {
                    val trimUser = username.trim()
                    val trimEmail = email.trim()
                    val trimPass = password.trim()

                    if (trimUser.isEmpty() || trimEmail.isEmpty()) {
                        Toast.makeText(context, "Username dan Email wajib diisi", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        try {
                            // 1. Update in Supabase
                            SupabaseManager.client.auth.updateUser {
                                this.email = trimEmail
                                if (trimPass.isNotEmpty()) {
                                    this.password = trimPass
                                }
                                this.data = buildJsonObject {
                                    put("username", trimUser)
                                }
                            }

                            // 2. Update in Local SQLite
                            dbHelper.updateUser(oldUsername, trimUser, trimEmail, trimPass, profileImageUri)

                            // 3. Update SharedPreferences Session
                            sharedPref.edit().apply {
                                putString("username", trimUser)
                                putString("email", trimEmail)
                                putString("profile_image", profileImageUri)
                                apply()
                            }

                            Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Gagal memperbarui profil: ${e.localizedMessage ?: "Terjadi kesalahan"}", Toast.LENGTH_LONG).show()
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
                    text = "Simpan Perubahan",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = OnPrimaryFixed
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delete Account Button (Styled with Red)
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFBA1A1A))
                    Text(
                        text = "Hapus Akun",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBA1A1A),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Hapus Akun Anda?",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OnSurface
                )
            },
            text = {
                Text(
                    text = "Tindakan ini akan menghapus akun Anda dari basis data lokal. Anda tidak akan dapat masuk kembali tanpa melakukan pendaftaran ulang. Apakah Anda yakin?",
                    fontFamily = ManropeFontFamily,
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        coroutineScope.launch {
                            try {
                                // 1. Delete locally in SQLite
                                dbHelper.deleteUser(oldUsername)
                                
                                // 2. Sign out from Supabase
                                SupabaseManager.client.auth.signOut()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            // 3. Reset User Session
                            sharedPref.edit().apply {
                                putBoolean("isLoggedIn", false)
                                putString("username", "")
                                putString("email", "")
                                putString("userId", "")
                                putString("profile_image", "")
                                apply()
                            }

                            Toast.makeText(context, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Hapus",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBA1A1A)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        text = "Batal",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Outline
                    )
                }
            },
            containerColor = SurfaceContainerLowest,
            shape = RoundedCornerShape(28.dp)
        )
    }
}
