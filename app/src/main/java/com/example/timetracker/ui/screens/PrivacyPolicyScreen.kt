package com.example.timetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.timetracker.ui.theme.ManropeFontFamily
import com.example.timetracker.ui.theme.OnSurface
import com.example.timetracker.ui.theme.OnSurfaceVariant
import com.example.timetracker.ui.theme.Primary
import com.example.timetracker.ui.theme.WarmBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmBackground
                )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Last updated: June 24, 2026",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            PolicySection(
                title = "1. Introduction",
                content = "Welcome to Momentum. We value your privacy and are committed to protecting your personal data. This privacy policy explains how we collect, use, and safe-keep your time tracking records, account credentials, and other personal information when you use our application."
            )

            PolicySection(
                title = "2. Data We Collect",
                content = "To provide you with high-quality time tracking and analytical tools, we collect the following:\n" +
                        "• Account Details: Username, email address, and encrypted passwords used for authentication.\n" +
                        "• Activity Logs: Details of time tracked, activity names, category tags, project names, and timestamp records.\n" +
                        "• Local Preferences: Light or dark mode themes and personalized time target goals."
            )

            PolicySection(
                title = "3. How We Use Your Data",
                content = "The collected data is exclusively used to:\n" +
                        "• Keep you logged in securely and synchronize your profile features.\n" +
                        "• Compile statistics, reports, and charts showing your daily and weekly progress.\n" +
                        "• Maintain and optimize the local database schema on your device."
            )

            PolicySection(
                title = "4. Data Storage and Security",
                content = "All information logged in the Momentum app is stored directly on your local device storage using a secure SQLite database. We do not sell or upload your personal time logs to any remote database servers without your explicit consent."
            )

            PolicySection(
                title = "5. Your Rights",
                content = "Since your data is stored locally, you have full control over it. You can clear all cached records or reset your account data by uninstalling the application or wiping the application's storage in your Android settings."
            )

            PolicySection(
                title = "6. Changes to this Policy",
                content = "We may update this Privacy Policy from time to time. Any changes will be published here with an updated revision date."
            )

            PolicySection(
                title = "7. Contact Us",
                content = "If you have any questions or feedback regarding our privacy practices, please contact us at support@momentum.io."
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PolicySection(title: String, content: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = OnSurface
        )
        Text(
            text = content,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = OnSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}
