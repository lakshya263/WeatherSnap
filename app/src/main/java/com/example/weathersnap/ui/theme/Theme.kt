package com.example.weathersnap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Define the dark color scheme using our colors from Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    background = Background,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error
)

@Composable
fun WeatherSnapTheme(
    content: @Composable () -> Unit    // everything inside the theme
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,       // from Type.kt
        content = content
    )
}