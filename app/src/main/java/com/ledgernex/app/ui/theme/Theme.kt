package com.ledgernex.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Couleurs LedgerNex
val BluePrimary = Color(0xFF1F3A8A)
val BlueDark = Color(0xFF152A6B)
val BlueLight = Color(0xFF3B5FCC)
val GreenAccent = Color(0xFF16A34A)
val GreenLight = Color(0xFF22C55E)
val RedError = Color(0xFFDC2626)
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceWhite = Color(0xFFFFFFFF)
val OnSurfaceDark = Color(0xFF1E293B)
val OnSurfaceSecondary = Color(0xFF64748B)

private val LedgerNexColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueLight,
    onPrimaryContainer = Color.White,
    secondary = GreenAccent,
    onSecondary = Color.White,
    secondaryContainer = GreenLight,
    background = BackgroundLight,
    surface = SurfaceWhite,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    error = RedError,
    onError = Color.White
)

@Composable
fun LedgerNexTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LedgerNexColorScheme,
        content = content
    )
}
