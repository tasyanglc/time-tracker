package com.example.timetracker.ui.theme

import androidx.compose.ui.graphics.Color

// === Warm Background ===
val WarmBackground: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF1B1A17) else Color(0xFFFDF9F0)

// === Primary (Dark Amber) ===
val Primary: Color
    get() = if (ThemeState.isDarkMode) Color(0xFFEBC32E) else Color(0xFF725C00)
val OnPrimary: Color
    get() = Color(0xFFFFFFFF)
val PrimaryContainer: Color
    get() = Color(0xFFFFD541)
val OnPrimaryContainer: Color
    get() = Color(0xFF725C00)
val OnPrimaryFixed: Color
    get() = Color(0xFF231B00)
val InversePrimary: Color
    get() = Color(0xFFEBC32E)

// === Secondary ===
val Secondary: Color
    get() = if (ThemeState.isDarkMode) Color(0xFFCBC6BA) else Color(0xFF605E58)
val OnSecondary: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF32302A) else Color(0xFFFFFFFF)
val SecondaryContainer: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF494740) else Color(0xFFE6E2D9)
val OnSecondaryContainer: Color
    get() = if (ThemeState.isDarkMode) Color(0xFFE6E2D9) else Color(0xFF66645E)

// === Tertiary (Cyan/Teal) ===
val Tertiary: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF80F4FF) else Color(0xFF006970)
val OnTertiary: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF00363B) else Color(0xFFFFFFFF)
val TertiaryContainer: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF004F55) else Color(0xFF31EFFE)
val OnTertiaryContainer: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF80F4FF) else Color(0xFF006971)
val TertiaryFixed: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF80F4FF) else Color(0xFF80F4FF)
val OnTertiaryFixed: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF002022) else Color(0xFF002022)

// === Error ===
val Error: Color
    get() = if (ThemeState.isDarkMode) Color(0xFFFFB4AB) else Color(0xFFBA1A1A)
val OnError: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF690005) else Color(0xFFFFFFFF)
val ErrorContainer: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF93000A) else Color(0xFFFFDAD6)
val OnErrorContainer: Color
    get() = if (ThemeState.isDarkMode) Color(0xFFFFDAD6) else Color(0xFF93000A)

// === Surface & Background ===
val Surface: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF1B1A17) else Color(0xFFFBF9F9)
val OnSurface: Color
    get() = if (ThemeState.isDarkMode) Color(0xFFEAE2D5) else Color(0xFF1B1C1C)
val SurfaceVariant: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF4D4634) else Color(0xFFE3E2E2)
val OnSurfaceVariant: Color
    get() = if (ThemeState.isDarkMode) Color(0xFFD0C6AD) else Color(0xFF4D4634)
val SurfaceContainerLowest: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF25231F) else Color(0xFFFFFFFF)
val SurfaceContainer: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF2C2A24) else Color(0xFFEFEDED)

// === Outline ===
val Outline: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF99907C) else Color(0xFF7F7661)
val OutlineVariant: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF4D4634) else Color(0xFFD0C6AD)
val CardBorder: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF3D3728) else Color(0xFFE5E1D8)

// === Inverse ===
val InverseSurface: Color
    get() = if (ThemeState.isDarkMode) Color(0xFFEAE2D5) else Color(0xFF303031)
val InverseOnSurface: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF303031) else Color(0xFFF2F0F0)

// === Misc ===
val CoffeeIconBg: Color
    get() = if (ThemeState.isDarkMode) Color(0xFF3D1E22) else Color(0xFFFFEBEE)
val CoffeeIconColor: Color
    get() = if (ThemeState.isDarkMode) Color(0xFFFF8A80) else Color(0xFFD32F2F)