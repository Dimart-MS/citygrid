package com.example.citygrid.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.citygrid.R
import com.example.citygrid.navigation.BottomNavItem
import com.example.citygrid.navigation.Screen
import com.example.citygrid.ui.theme.AmberAccent
import com.example.citygrid.ui.theme.CityGridPrimary
import com.example.citygrid.ui.theme.CityGridPrimaryDark
import com.example.citygrid.ui.theme.DividerColor
import com.example.citygrid.ui.theme.TextPrimary
import com.example.citygrid.ui.theme.TextSecondary
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.StatusBlue
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.Elevation
import com.example.citygrid.ui.theme.Radius
import com.example.citygrid.ui.theme.Spacing
import com.example.citygrid.ui.theme.IconSize
import com.example.citygrid.ui.theme.AvatarSize
import com.example.citygrid.ui.theme.BorderWidth
import com.example.citygrid.ui.theme.Motion
import com.example.citygrid.ui.theme.brandGradient
import com.example.citygrid.ui.theme.GradientNavyStart
import com.example.citygrid.ui.theme.GradientNavyEnd

// ─── Iconos personalizados Canvas ─────────────────────────────────────────────
@Composable
fun IconoPersonalizado(name: String, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        when (name) {
            "lightbulb" -> {
                val path = Path().apply {
                    arcTo(
                        rect = Rect(w * 0.25f, h * 0.12f, w * 0.75f, h * 0.62f),
                        startAngleDegrees = -30f,
                        sweepAngleDegrees = 240f,
                        forceMoveTo = false
                    )
                    lineTo(w * 0.38f, h * 0.75f)
                    lineTo(w * 0.62f, h * 0.75f)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 2.dp.toPx()))
                drawLine(color = tint, start = Offset(w * 0.38f, h * 0.82f), end = Offset(w * 0.62f, h * 0.82f), strokeWidth = 2.dp.toPx())
                drawLine(color = tint, start = Offset(w * 0.44f, h * 0.90f), end = Offset(w * 0.56f, h * 0.90f), strokeWidth = 2.dp.toPx())
            }
            "moon" -> {
                val path = Path().apply {
                    moveTo(w * 0.65f, h * 0.15f)
                    arcTo(
                        rect = Rect(w * 0.15f, h * 0.15f, w * 0.85f, h * 0.85f),
                        startAngleDegrees = -70f,
                        sweepAngleDegrees = 140f,
                        forceMoveTo = false
                    )
                    arcTo(
                        rect = Rect(w * 0.38f, h * 0.22f, w * 0.88f, h * 0.78f),
                        startAngleDegrees = 70f,
                        sweepAngleDegrees = -140f,
                        forceMoveTo = false
                    )
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 2.dp.toPx()))
            }
            "sun" -> {
                drawCircle(color = tint, radius = w * 0.2f, style = Stroke(width = 2.dp.toPx()))
                val rayLength = w * 0.12f
                val innerR = w * 0.25f
                for (i in 0 until 8) {
                    val angle = i * Math.PI / 4
                    val startX = (w * 0.5f + Math.cos(angle) * innerR).toFloat()
                    val startY = (h * 0.5f + Math.sin(angle) * innerR).toFloat()
                    val endX = (w * 0.5f + Math.cos(angle) * (innerR + rayLength)).toFloat()
                    val endY = (h * 0.5f + Math.sin(angle) * (innerR + rayLength)).toFloat()
                    drawLine(color = tint, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 2.dp.toPx())
                }
            }
            "monitor" -> {
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(w * 0.12f, h * 0.18f, w * 0.88f, h * 0.68f),
                            radiusX = 3.dp.toPx(), radiusY = 3.dp.toPx()
                        )
                    )
                }
                drawPath(path, color = tint, style = Stroke(width = 2.dp.toPx()))
                drawLine(color = tint, start = Offset(w * 0.5f, h * 0.68f), end = Offset(w * 0.5f, h * 0.85f), strokeWidth = 2.dp.toPx())
                drawLine(color = tint, start = Offset(w * 0.32f, h * 0.85f), end = Offset(w * 0.68f, h * 0.85f), strokeWidth = 2.dp.toPx())
            }
            "clock" -> {
                drawCircle(color = tint, radius = w * 0.4f, style = Stroke(width = 2.dp.toPx()))
                drawLine(color = tint, start = Offset(w * 0.5f, h * 0.5f), end = Offset(w * 0.5f, h * 0.24f), strokeWidth = 2.dp.toPx())
                drawLine(color = tint, start = Offset(w * 0.5f, h * 0.5f), end = Offset(w * 0.72f, h * 0.5f), strokeWidth = 2.dp.toPx())
            }
            "water" -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.12f)
                    cubicTo(w * 0.38f, h * 0.3f, w * 0.18f, h * 0.58f, w * 0.18f, h * 0.74f)
                    arcTo(
                        rect = Rect(w * 0.18f, h * 0.5f, w * 0.82f, h * 0.9f),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                    cubicTo(w * 0.82f, h * 0.58f, w * 0.62f, h * 0.3f, w * 0.5f, h * 0.12f)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 2.dp.toPx()))
            }
            "plastic" -> {
                val path = Path().apply {
                    moveTo(w * 0.42f, h * 0.15f)
                    lineTo(w * 0.58f, h * 0.15f)
                    lineTo(w * 0.58f, h * 0.24f)
                    lineTo(w * 0.42f, h * 0.24f)
                    close()
                    moveTo(w * 0.34f, h * 0.24f)
                    lineTo(w * 0.66f, h * 0.24f)
                    lineTo(w * 0.74f, h * 0.44f)
                    lineTo(w * 0.74f, h * 0.85f)
                    lineTo(w * 0.26f, h * 0.85f)
                    lineTo(w * 0.26f, h * 0.44f)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = 2.dp.toPx()))
            }
            "trash" -> {
                val path = Path().apply {
                    moveTo(w * 0.25f, h * 0.25f)
                    lineTo(w * 0.75f, h * 0.25f)
                    moveTo(w * 0.45f, h * 0.25f)
                    lineTo(w * 0.45f, h * 0.18f)
                    lineTo(w * 0.55f, h * 0.18f)
                    lineTo(w * 0.55f, h * 0.25f)
                    moveTo(w * 0.3f, h * 0.25f)
                    lineTo(w * 0.35f, h * 0.85f)
                    lineTo(w * 0.65f, h * 0.85f)
                    lineTo(w * 0.7f, h * 0.25f)
                }
                drawPath(path, color = tint, style = Stroke(width = 2.dp.toPx()))
            }
            "leaf" -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    cubicTo(w * 0.8f, h * 0.25f, w * 0.85f, h * 0.6f, w * 0.5f, h * 0.85f)
                    cubicTo(w * 0.15f, h * 0.6f, w * 0.2f, h * 0.25f, w * 0.5f, h * 0.15f)
                    moveTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.5f, h * 0.85f)
                }
                drawPath(path, color = tint, style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}

// ─── Modificador de superficie interactiva unificada ──────────────────────────
/**
 * Proporciona feedback visual consistente para elementos interactivos:
 * - Escala al presionar (0.97x)
 * - Alpha al presionar (0.85)
 * - Animación suave con spring
 *
 * Uso: Modifier.interactiveSurface().clickable { ... }
 */
fun Modifier.interactiveSurface(): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "pressScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(durationMillis = Motion.durationFast),
        label = "pressAlpha"
    )

    this.scale(scale).alpha(alpha)
}

// ─── Tarjeta de módulo reutilizable ───────────────────────────────────────────
/**
 * Tarjeta estandarizada para módulos del dashboard y pantallas de detalle.
 * Garantiza consistencia visual en bordes, padding, elevación y forma.
 */
@Composable
fun ModuleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .border(BorderWidth.thin, DividerColor, RoundedCornerShape(Radius.xxl))
        .then(
            if (onClick != null) {
                Modifier
                    .interactiveSurface()
                    .clickable(onClick = onClick, role = Role.Button)
            } else Modifier
        )

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(Radius.xxl),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.md),
        content = content
    )
}

// ─── Header global CityGrid v3 — diseño limpio tipo Linear ──────────────────
@Composable
fun BloqueEncabezado(
    alertasPendientes: Int = 0,
    connectionState: com.example.citygrid.data.MqttConnectionState = com.example.citygrid.data.MqttConnectionState.DISCONNECTED,
    latenciaMs: Long = 0L,
    onLogoutClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    onConfigClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = Radius.xl, bottomEnd = Radius.xl),
        color = Color.Transparent,
        shadowElevation = Elevation.lg,
        tonalElevation = Elevation.none
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(colors = brandGradient()),
                    shape = RoundedCornerShape(bottomStart = Radius.xl, bottomEnd = Radius.xl)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logosinfondo),
                    contentDescription = "Logo CityGrid",
                    modifier = Modifier.size(AvatarSize.lg),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(Spacing.md))

                // Título + estado
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CityGrid",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.3).sp
                    )
                    val (statusText, statusColor) = when (connectionState) {
                        com.example.citygrid.data.MqttConnectionState.CONNECTED -> Pair(
                            if (latenciaMs > 0) "En línea · ${latenciaMs}ms" else "En línea",
                            StatusGreen
                        )
                        com.example.citygrid.data.MqttConnectionState.CONNECTING -> Pair(
                            "Conectando...",
                            AmberAccent
                        )
                        com.example.citygrid.data.MqttConnectionState.DEVICE_OFFLINE -> Pair(
                            "ESP32 desconectado",
                            StatusRed
                        )
                        com.example.citygrid.data.MqttConnectionState.DISCONNECTED -> Pair(
                            "Sin conexión",
                            StatusRed
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = statusText,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Acciones
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Notificaciones
                    if (alertasPendientes > 0) {
                        Box(
                            modifier = Modifier
                                .size(AvatarSize.md)
                                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                                .interactiveSurface()
                                .clickable { onBellClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alertas",
                                tint = AmberAccent,
                                modifier = Modifier.size(IconSize.lg)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(14.dp)
                                    .background(AmberAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (alertasPendientes > 9) "9+" else "$alertasPendientes",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(Spacing.sm))
                    }

                    // Configuración
                    Box(
                        modifier = Modifier
                            .size(AvatarSize.md)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            .interactiveSurface()
                            .clickable { onConfigClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(IconSize.lg)
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.sm))

                    // Logout
                    Box(
                        modifier = Modifier
                            .size(AvatarSize.md)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            .interactiveSurface()
                            .clickable { onLogoutClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(IconSize.lg)
                        )
                    }
                }
            }
        }
    }
}

// ─── Barra de navegación inferior flotante v3 ────────────────────────────────
@Composable
fun BarraNavegacionInferiorCompartida(navController: NavController, currentRoute: String?) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Residuos,
        BottomNavItem.Agua,
        BottomNavItem.Alumbrado,
        BottomNavItem.Alertas
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        shape = RoundedCornerShape(Radius.lg),
        color = CityGridPrimaryDark,
        shadowElevation = Elevation.lg,
        tonalElevation = Elevation.none
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isActive = currentRoute == item.route
                ElementoNavegacionInferiorCompartido(
                    icon = item.iconName,
                    label = item.title,
                    isActive = isActive,
                    onClick = {
                        if (item.route == Screen.Dashboard.route) {
                            navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                        } else {
                            navController.navigate(item.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RowScope.ElementoNavegacionInferiorCompartido(
    icon: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val tintColor by animateColorAsState(
        targetValue = if (isActive) Color.White else Color.White.copy(alpha = 0.45f),
        animationSpec = tween(durationMillis = Motion.durationNormal),
        label = "navTint"
    )

    val bgAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.18f else 0f,
        animationSpec = tween(durationMillis = Motion.durationNormal),
        label = "navBgAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .interactiveSurface()
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .background(
                    color = Color.White.copy(alpha = bgAlpha),
                    shape = RoundedCornerShape(Radius.md)
                )
        ) {
            when (icon) {
                "home" -> Icon(Icons.Default.Home, label, modifier = Modifier.size(IconSize.xxl), tint = tintColor)
                "trash" -> Icon(Icons.Default.Delete, label, modifier = Modifier.size(IconSize.xxl), tint = tintColor)
                "water" -> IconoPersonalizado("water", tintColor, Modifier.size(IconSize.xxl))
                "sun" -> IconoPersonalizado("sun", tintColor, Modifier.size(IconSize.xxl))
                "bell" -> Icon(Icons.Default.Notifications, label, modifier = Modifier.size(IconSize.xxl), tint = tintColor)
            }
        }
        Text(
            text = label,
            color = tintColor,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            letterSpacing = 0.2.sp
        )
    }
}

// ─── Barra de Progreso mejorada ────────────────────────────────────────────────
@Composable
fun BarraProgresoPersonalizada(progress: Float, color: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "progressBarAnimation"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(DividerColor, RoundedCornerShape(percent = 50))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(color.copy(alpha = 0.8f), color)
                    ),
                    shape = RoundedCornerShape(percent = 50)
                )
        )
    }
}

// ─── Elemento de lista de eventos ─────────────────────────────────────────────
@Composable
fun ElementoEventoLista(title: String, subtitle: String, dotColor: Color = CityGridPrimary) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(10.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(Spacing.md))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

// ─── Extensión bounceClick para respuesta táctil interactiva ───────────────────
fun Modifier.bounceClick(scaleDown: Float = 0.96f) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )

    this
        .scale(scale)
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    isPressed = false
                }
            }
        }
}

// ─── Tarjeta común para Advertencia de Conectividad (Offline State) ───────────
@Composable
fun TarjetaAdvertenciaConectividad(
    segundosInactivo: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAlpha")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = Motion.durationSlow),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderWidth.thin, StatusRed.copy(alpha = 0.25f), RoundedCornerShape(Radius.lg)),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = StatusRed.copy(alpha = 0.04f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .alpha(alphaAnim)
                    .size(AvatarSize.xl)
                    .background(StatusRed.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Dispositivo inactivo",
                    tint = StatusRed,
                    modifier = Modifier.size(IconSize.xl)
                )
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column {
                Text(
                    text = "Dispositivo Inactivo",
                    color = StatusRed,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (segundosInactivo > 0)
                        "No se han recibido lecturas del ESP32 hace $segundosInactivo segundos."
                    else
                        "No se han recibido lecturas recientes del ESP32.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ─── ShimmerPlaceholder para Efecto de Carga Esqueleto ───────────────────────
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified,
    height: androidx.compose.ui.unit.Dp = 20.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim + 200f, translateAnim + 200f)
    )

    val sizeModifier = if (width == androidx.compose.ui.unit.Dp.Unspecified) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.width(width)
    }

    Box(
        modifier = modifier
            .then(sizeModifier)
            .height(height)
            .background(brush, shape)
    )
}
