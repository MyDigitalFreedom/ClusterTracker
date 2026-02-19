package com.clustertracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue20,
    onPrimaryContainer = Blue80,
    secondary = Green40,
    onSecondary = Color.White,
    secondaryContainer = Green20,
    onSecondaryContainer = Green80,
    error = Red40,
    onError = Color.White,
    errorContainer = Red20,
    onErrorContainer = Red80,
    background = DarkBackground,
    onBackground = Color(0xFFE0E0E0),
    surface = DarkSurface,
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBDBDBD)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue20,
    onPrimary = Color.White,
    primaryContainer = Blue80,
    onPrimaryContainer = Blue20,
    secondary = Green20,
    onSecondary = Color.White,
    secondaryContainer = Green80,
    onSecondaryContainer = Green20,
    error = Red20,
    onError = Color.White,
    errorContainer = Red80,
    onErrorContainer = Red20,
    background = LightBackground,
    onBackground = Color(0xFF1A1A1A),
    surface = LightSurface,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF424242)
)

@Composable
fun ClusterTrackerTheme(
    darkTheme: Boolean = true, // default dark — attacks often at night
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
