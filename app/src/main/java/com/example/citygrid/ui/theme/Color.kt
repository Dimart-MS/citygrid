package com.example.citygrid.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Paleta principal CityGrid v3 — identidad Linear/Vercel ──────────────────
val CityGridPrimary      = Color(0xFF2563EB)   // Blue-600 — botones, activos, iconos
val CityGridPrimaryLight = Color(0xFFDBEAFE)   // Blue-100 — fondos de badges/chips
val CityGridPrimaryDark  = Color(0xFF0F1B2D)   // Navy profundo — header, nav bar
val CityGridPrimaryMid   = Color(0xFF1D4ED8)   // Blue-700 — acentos secundarios

// ─── Degradado marca ─────────────────────────────────────────────────────────
val GradientNavyStart = CityGridPrimaryDark     // 0xFF0F1B2D
val GradientNavyMid   = Color(0xFF162840)
val GradientNavyEnd   = Color(0xFF1A3356)

fun brandGradient() = listOf(GradientNavyStart, GradientNavyMid, GradientNavyEnd)

// ─── Acento ámbar ───────────────────────────────────────────────────────────
val AmberAccent          = Color(0xFFD97706)   // Amber-600 — alertas
val AmberLight           = Color(0xFFFEF3C7)   // Amber-50
val AmberDark            = Color(0xFF92400E)   // Amber-800

// ─── Estados de sensores ────────────────────────────────────────────────────
val StatusRed            = Color(0xFFDC2626)   // Red-600
val StatusYellow         = Color(0xFFD97706)   // Amber-600
val StatusGreen          = Color(0xFF059669)   // Emerald-600
val StatusBlue           = Color(0xFF2563EB)   // Blue-600

// ─── Fondo y superficies ────────────────────────────────────────────────────
val BackgroundLight      = Color(0xFFF8FAFC)   // Slate-50 — fondo general más neutro
val SurfaceCard          = Color(0xFFFFFFFF)   // Blanco
val SurfaceElevated      = Color(0xFFF1F5F9)   // Slate-100 — tarjetas secundarias
val SurfaceContainer     = Color(0xFFF8FAFC)   // Slate-50
val TextPrimary          = Color(0xFF0F172A)   // Slate-900
val TextSecondary        = Color(0xFF475569)   // Slate-600 — más contraste que 64748B
val DividerColor         = Color(0xFFE2E8F0)   // Slate-200 — bordes visibles

// ─── Tema oscuro ────────────────────────────────────────────────────────────
val BackgroundDark       = Color(0xFF0B1121)   // Navy casi negro
val SurfaceDark          = Color(0xFF111B2E)
val SurfaceVariantDark   = Color(0xFF1A2740)

// ─── Tokens semánticos ──────────────────────────────────────────────────────
val TextTertiary         = Color(0xFF94A3B8)   // Slate-400
val TextOnPrimary        = Color(0xFFFFFFFF)
val TextOnDark           = Color(0xFFF1F5F9)   // Slate-100

// ─── Overlay ────────────────────────────────────────────────────────────────
val OverlayLight         = Color(0x0A000000)   // 4% negro — más sutil
val OverlayDark          = Color(0x0AFFFFFF)
val BackdropScrim        = Color(0x80000000)

// ─── Feedback semántico ─────────────────────────────────────────────────────
val SuccessGreen         = Color(0xFF059669)
val SuccessLight         = Color(0xFFD1FAE5)
val WarningAmber         = Color(0xFFD97706)
val WarningLight         = Color(0xFFFEF3C7)
val ErrorRed             = Color(0xFFDC2626)
val ErrorLight           = Color(0xFFFEE2E2)
val InfoBlue             = Color(0xFF2563EB)
val InfoLight            = Color(0xFFDBEAFE)

// ─── Tema oscuro — textos y bordes ─────────────────────────────────────────
val TextPrimaryDark      = Color(0xFFF1F5F9)
val TextSecondaryDark    = Color(0xFF94A3B8)
val DividerDark          = Color(0xFF1E293B)
val BorderDark           = Color(0xFF334155)

// ─── Sombras tintadas (para usar con Modifier.shadow) ───────────────────────
val ShadowColorLight     = Color(0x1A0F172A)   // 10% slate-900
val ShadowColorDark      = Color(0x33000000)

// ─── Fondos de iconos circulares ────────────────────────────────────────────
val IconBgBlue           = Color(0xFFDBEAFE)
val IconBgAmber          = Color(0xFFFEF3C7)
val IconBgRed            = Color(0xFFFEE2E2)
val IconBgGreen          = Color(0xFFD1FAE5)
val IconBgPurple         = Color(0xFFEDE9FE)
val IconBgSlate          = Color(0xFFF1F5F9)
