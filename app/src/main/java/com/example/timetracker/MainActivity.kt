package com.example.timetracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timetracker.ui.screens.*
import com.example.timetracker.ui.theme.ThemeState
import com.example.timetracker.ui.theme.TimetrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load dark mode setting
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        ThemeState.isDarkMode = sharedPref.getBoolean("isDarkMode", false)

        setContent {
            TimetrackerTheme(darkTheme = ThemeState.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") { SplashScreen(navController) }
                        composable("onboarding") { OnboardingScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
                        composable("dashboard") { HomeScreen(navController) }
                        composable("add_edit") { AddEditScreen(navController) }
                        composable("privacy_policy") { PrivacyPolicyScreen(navController) }
                    }
                }
            }
        }
    }
}