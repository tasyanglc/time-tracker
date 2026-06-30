package com.example.timetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity

// Global state for Dark Mode
object ThemeState {
    var isDarkMode by mutableStateOf(false)
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFEBC32E), // InversePrimary (Warm gold)
    onPrimary = Color(0xFF3B2F00),
    primaryContainer = Color(0xFF554500),
    onPrimaryContainer = Color(0xFFFFE07D),
    secondary = Color(0xFFCBC6BA),
    onSecondary = Color(0xFF32302A),
    secondaryContainer = Color(0xFF494740),
    onSecondaryContainer = Color(0xFFE6E2D9),
    tertiary = Color(0xFF80F4FF),
    onTertiary = Color(0xFF00363B),
    tertiaryContainer = Color(0xFF004F55),
    onTertiaryContainer = Color(0xFF80F4FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1B1A17), // Dark Warm background
    onBackground = Color(0xFFEAE2D5),
    surface = Color(0xFF1B1A17),
    onSurface = Color(0xFFEAE2D5),
    surfaceVariant = Color(0xFF4D4634),
    onSurfaceVariant = Color(0xFFD0C6AD),
    outline = Color(0xFF99907C),
    outlineVariant = Color(0xFF4D4634),
    inverseSurface = Color(0xFFEAE2D5),
    inverseOnSurface = Color(0xFF303031),
    inversePrimary = Primary,
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = WarmBackground,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary,
)

@Composable
fun TimetrackerTheme(
    darkTheme: Boolean = ThemeState.isDarkMode,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}