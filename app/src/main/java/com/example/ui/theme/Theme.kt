package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AuraDarkColorScheme = darkColorScheme(
    primary = AuraNeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00363A),
    onPrimaryContainer = AuraNeonCyan,
    secondary = AuraElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3C1361),
    onSecondaryContainer = Color(0xFFE0AAFF),
    tertiary = AuraGoldHiRes,
    onTertiary = Color.Black,
    background = AuraBackgroundDark,
    onBackground = AuraTextPrimary,
    surface = AuraSurfaceDark,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraSurfaceVariantDark,
    onSurfaceVariant = AuraTextSecondary,
    outline = AuraCardBorder
)

@Composable
fun AuraMusicTheme(
    darkTheme: Boolean = true, // Force dark mode for premium music player experience
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AuraDarkColorScheme,
        typography = Typography,
        content = content
    )
}

