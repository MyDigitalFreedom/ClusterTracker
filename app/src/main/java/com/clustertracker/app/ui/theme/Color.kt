package com.clustertracker.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette — calming blue tones
val Blue80 = Color(0xFFB3D1FF)
val Blue40 = Color(0xFF4A7FD4)
val Blue20 = Color(0xFF2A5298)

// Secondary — green for O2 therapy
val Green80 = Color(0xFFA5D6A7)
val Green40 = Color(0xFF4CAF50)
val Green20 = Color(0xFF2E7D32)

// Error/Pain — red tones
val Red80 = Color(0xFFFFB3B3)
val Red40 = Color(0xFFFF6B6B)
val Red20 = Color(0xFFD32F2F)

// Surfaces — dark theme first
val DarkBackground = Color(0xFF0E0E0E)
val DarkSurface = Color(0xFF1A1A1A)
val DarkSurfaceVariant = Color(0xFF262626)

val LightBackground = Color(0xFFFEFEFE)
val LightSurface = Color(0xFFF5F5F5)
val LightSurfaceVariant = Color(0xFFE8E8E8)

// KIP pain scale colors
val PainNone = Color(0xFF4CAF50)       // 0 — green
val PainMild = Color(0xFF8BC34A)       // 1-3
val PainModerate = Color(0xFFFFC107)   // 4-6 — yellow/amber
val PainSevere = Color(0xFFFF9800)     // 7-8 — orange
val PainExcruciating = Color(0xFFFF5722) // 9-10 — deep red/orange

fun painColor(intensity: Int): Color = when (intensity) {
    0 -> PainNone
    in 1..3 -> PainMild
    in 4..6 -> PainModerate
    in 7..8 -> PainSevere
    else -> PainExcruciating
}
