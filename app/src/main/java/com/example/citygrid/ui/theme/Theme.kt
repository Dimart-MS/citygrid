package com.example.citygrid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ─── Esquema Claro (default CityGrid) ─────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary        = CityGridGreen,
    onPrimary      = SurfaceCard,
    primaryContainer = CityGridGreenDark,
    onPrimaryContainer = SurfaceCard,
    secondary      = CityGridGreenLight,
    onSecondary    = SurfaceCard,
    tertiary       = StatusBlue,
    error          = StatusRed,
    onError        = SurfaceCard,
    background     = BackgroundLight,
    onBackground   = TextPrimary,
    surface        = SurfaceCard,
    onSurface      = TextPrimary,
    surfaceVariant = DividerColor,
    onSurfaceVariant = TextSecondary,
    outline        = DividerColor,
)

// ─── Esquema Oscuro ───────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary        = CityGridGreenLight,
    onPrimary      = TextPrimary,
    primaryContainer = CityGridGreenDark,
    onPrimaryContainer = SurfaceCard,
    secondary      = CityGridGreenLight,
    onSecondary    = TextPrimary,
    tertiary       = StatusBlue,
    error          = StatusRed,
    onError        = SurfaceCard,
    background     = BackgroundDark,
    onBackground   = SurfaceCard,
    surface        = SurfaceDark,
    onSurface      = SurfaceCard,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = DividerColor,
    outline        = TextSecondary,
)

@Composable
fun CityGridTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
