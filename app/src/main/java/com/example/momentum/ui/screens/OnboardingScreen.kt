package com.example.momentum.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.momentum.R
import com.example.momentum.ui.theme.ManropeFontFamily
import com.example.momentum.ui.theme.OnPrimaryFixed
import com.example.momentum.ui.theme.OnSurface
import com.example.momentum.ui.theme.OnSurfaceVariant
import com.example.momentum.ui.theme.Primary
import com.example.momentum.ui.theme.PrimaryContainer
import com.example.momentum.ui.theme.WarmBackground

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current

    val completeOnboarding = {
        val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("isFirstRun", false).apply()
        navController.navigate("login") {
            popUpTo("onboarding") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Visual element
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(PrimaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(id = R.string.onboarding_title),
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                color = OnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.onboarding_desc),
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Bottom Next button
        Button(
            onClick = { completeOnboarding() },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_next),
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = OnPrimaryFixed
            )
        }
    }
}
