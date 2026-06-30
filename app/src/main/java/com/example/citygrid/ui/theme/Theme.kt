package com.example.citygrid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ─── Esquema Claro (default CityGrid) ─────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary        = CityGridPrimary,
    onPrimary      = SurfaceCard,
    primaryContainer = CityGridPrimaryDark,
    onPrimaryContainer = SurfaceCard,
    secondary      = CityGridPrimaryLight,
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
    primary        = CityGridPrimaryLight,
    onPrimary      = TextPrimary,
    primaryContainer = CityGridPrimaryDark,
    onPrimaryContainer = SurfaceCard,
    secondary      = CityGridPrimaryLight,
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
    darkTheme: Boolean = false, // Siempre light mode
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
