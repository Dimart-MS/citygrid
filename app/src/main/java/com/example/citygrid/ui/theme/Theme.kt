package com.example.citygrid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ─── Esquema Claro CityGrid v2 ────────────────────────────────────────────────
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
    errorContainer         = StatusRed.copy(alpha = 0.12f),
    onErrorContainer       = StatusRed,
    background             = BackgroundLight,
    onBackground           = TextPrimary,
    surface                = SurfaceCard,
    onSurface              = TextPrimary,
    surfaceVariant         = DividerColor,
    onSurfaceVariant       = TextSecondary,
    surfaceContainer       = SurfaceContainer,
    surfaceContainerLow    = SurfaceElevated,
    surfaceContainerHigh   = DividerColor,
    outline                = DividerColor,
    outlineVariant         = CityGridPrimaryLight,
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
    onBackground           = SurfaceCard,
    surface                = SurfaceDark,
    onSurface              = SurfaceCard,
    surfaceVariant         = SurfaceVariantDark,
    onSurfaceVariant       = DividerColor,
    outline                = TextSecondary,
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
