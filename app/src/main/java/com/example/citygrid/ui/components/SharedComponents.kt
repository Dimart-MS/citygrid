package com.example.citygrid.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

// ─── Header global CityGrid v2 ────────────────────────────────────────────────
@Composable
fun BloqueEncabezado(
    alertasPendientes: Int = 0,
    onLogoutClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                ambientColor = CityGridPrimaryDark.copy(alpha = 0.25f),
                spotColor = CityGridPrimaryDark.copy(alpha = 0.35f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        CityGridPrimaryDark,
                        Color(0xFF0A3D62),
                        Color(0xFF0D5A8C)
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
    ) {
        // Patrón decorativo sutil — líneas diagonales
        Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
            val lineColor = Color.White.copy(alpha = 0.04f)
            val step = 24.dp.toPx()
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x + size.height, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                x += step
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo con halo
            Box(
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color.White.copy(alpha = 0.08f),
                            CircleShape
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.logosinfondo),
                    contentDescription = "Logo CityGrid",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "CityGrid",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "Smart City Platform",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón de notificaciones (con badge si hay alertas)
            if (alertasPendientes > 0) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alertas",
                        tint = AmberAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .size(16.dp)
                            .background(AmberAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (alertasPendientes > 9) "9+" else "$alertasPendientes",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Botón de logout
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { onLogoutClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Cerrar sesión",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Pill Navigation Inferior v2 ──────────────────────────────────────────────
@Composable
fun BarraNavegacionInferiorCompartida(navController: NavController, currentRoute: String?) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Residuos,
        BottomNavItem.Agua,
        BottomNavItem.Alumbrado,
        BottomNavItem.Alertas
    )
    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = CityGridPrimaryDark.copy(alpha = 0.4f),
                spotColor = CityGridPrimaryDark.copy(alpha = 0.5f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(CityGridPrimaryDark, Color(0xFF0A3D62))
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
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
                                popUpTo(Screen.Dashboard.route) {
                                    saveState = true
                                }
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
        animationSpec = tween(durationMillis = 250),
        label = "tintColor"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isActive) 1.18f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    val pillAlpha by animateFloatAsState(
        targetValue = if (isActive) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 250),
        label = "pillAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(34.dp)
                .width(54.dp)
                .background(
                    color = CityGridPrimary.copy(alpha = 0.2f * pillAlpha),
                    shape = RoundedCornerShape(17.dp)
                )
                .border(
                    width = if (isActive) 1.dp else 0.dp,
                    color = CityGridPrimary.copy(alpha = 0.4f * pillAlpha),
                    shape = RoundedCornerShape(17.dp)
                )
        ) {
            Box(
                modifier = Modifier.scale(iconScale),
                contentAlignment = Alignment.Center
            ) {
                when (icon) {
                    "home" -> Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = label,
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )
                    "trash" -> Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = label,
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )
                    "water" -> IconoPersonalizado(
                        name = "water",
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )
                    "sun" -> IconoPersonalizado(
                        name = "sun",
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )
                    "bell" -> Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = label,
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = tintColor,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            letterSpacing = 0.3.sp
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
        Spacer(modifier = Modifier.width(12.dp))
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
