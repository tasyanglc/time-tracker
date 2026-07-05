package com.example.momentum

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.momentum.ui.screens.*
import com.example.momentum.ui.theme.ThemeState
import com.example.momentum.ui.theme.MomentumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load dark mode setting
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        ThemeState.isDarkMode = sharedPref.getBoolean("isDarkMode", false)

        setContent {
            MomentumTheme(darkTheme = ThemeState.isDarkMode) {
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
                        composable("notification_settings") { NotificationSettingsScreen(navController) }
                        composable(
                            route = "add_edit?activityId={activityId}",
                            arguments = listOf(
                                navArgument("activityId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val activityId = backStackEntry.arguments
                                ?.getString("activityId")
                                ?.toIntOrNull()
                            AddEditScreen(navController, activityId)
                        }
                        composable("privacy_policy") { PrivacyPolicyScreen(navController) }
                        composable("edit_profile") { EditProfileScreen(navController) }
                        composable(
                            route = "activity_detail/{activityId}",
                            arguments = listOf(
                                navArgument("activityId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val activityId = backStackEntry.arguments?.getInt("activityId") ?: -1
                            ActivityDetailScreen(navController, activityId)
                        }
                    }
                }
            }
        }
    }
}