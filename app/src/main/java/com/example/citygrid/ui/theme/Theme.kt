package com.example.citygrid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Esquema Claro CityGrid v3 ────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary                = CityGridPrimary,
    onPrimary              = SurfaceCard,
    primaryContainer       = CityGridPrimaryLight,
    onPrimaryContainer     = CityGridPrimaryMid,
    secondary              = CityGridPrimaryMid,
    onSecondary            = SurfaceCard,
    secondaryContainer     = CityGridPrimaryLight,
    onSecondaryContainer   = CityGridPrimaryDark,
    tertiary               = AmberAccent,
    onTertiary             = SurfaceCard,
    tertiaryContainer      = AmberLight,
    onTertiaryContainer    = AmberDark,
    error                  = StatusRed,
    onError                = SurfaceCard,
    errorContainer         = ErrorLight,
    onErrorContainer       = StatusRed,
    background             = BackgroundLight,
    onBackground           = TextPrimary,
    surface                = SurfaceCard,
    onSurface              = TextPrimary,
    surfaceVariant         = SurfaceElevated,
    onSurfaceVariant       = TextSecondary,
    surfaceContainerLow    = SurfaceContainer,
    surfaceContainer       = SurfaceElevated,
    surfaceContainerHigh   = DividerColor,
    outline                = DividerColor,
    outlineVariant         = Color(0xFFE2E8F0),   // Slate-200
    inverseSurface         = CityGridPrimaryDark,
    inverseOnSurface       = SurfaceCard,
    inversePrimary         = CityGridPrimaryLight,
)

// ─── Esquema Oscuro ───────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary                = CityGridPrimary,
    onPrimary              = TextPrimary,
    primaryContainer       = CityGridPrimaryDark,
    onPrimaryContainer     = CityGridPrimaryLight,
    secondary              = CityGridPrimaryLight,
    onSecondary            = TextPrimary,
    tertiary               = AmberAccent,
    onTertiary             = BackgroundDark,
    error                  = StatusRed,
    onError                = SurfaceCard,
    background             = BackgroundDark,
    onBackground           = TextPrimaryDark,
    surface                = SurfaceDark,
    onSurface              = TextPrimaryDark,
    surfaceVariant         = SurfaceVariantDark,
    onSurfaceVariant       = TextSecondaryDark,
    outline                = BorderDark,
    outlineVariant         = DividerDark,
    inverseSurface         = SurfaceCard,
    inverseOnSurface       = TextPrimary,
    inversePrimary         = CityGridPrimaryDark,
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
