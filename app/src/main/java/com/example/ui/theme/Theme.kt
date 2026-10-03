package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccentGlow,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0E3A53),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = EmeraldSuccess,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = AmberWarning,
    onTertiary = Color.Black,
    background = MedicalDarkBg,
    onBackground = TextPrimary,
    surface = MedicalSurface,
    onSurface = TextPrimary,
    surfaceVariant = MedicalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = MedicalBorder,
    error = CrimsonError,
    onError = Color.White
)

@Composable
fun NeetPrepTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
