package com.example.citygrid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
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
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
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

@Composable
fun BloqueEncabezado(onNotificationClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A47))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logosinfondo),
                contentDescription = "Logo",
                modifier = Modifier.size(45.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "CityGrid",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Smart City Platform",
                    color = Color(0xFFCBD5E0),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Alertas",
                    tint = Color(0xFF1E3A47),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun BarraNavegacionInferiorCompartida(navController: NavController, currentRoute: String?) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Residuos,
        BottomNavItem.Agua,
        BottomNavItem.Alumbrado,
        BottomNavItem.Alertas
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
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
                        if (!isActive) {
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
fun ElementoNavegacionInferiorCompartido(icon: String, label: String, isActive: Boolean, onClick: () -> Unit) {
    val tintColor = if (isActive) Color(0xFF00A8CC) else Color(0xFF718096)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .width(60.dp)
            .clickable { onClick() }
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
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = tintColor,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
        if (isActive) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(Color(0xFF00A8CC), CircleShape)
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun BarraProgresoPersonalizada(progress: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color(0xFFEDF2F7), RoundedCornerShape(percent = 50))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(color, RoundedCornerShape(percent = 50))
        )
    }
}

@Composable
fun ElementoEventoLista(title: String, subtitle: String, dotColor: Color = Color(0xFF00A8CC)) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = Color(0xFF2D3748),
                fontSize = 13.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = Color(0xFF718096), fontSize = 10.sp)
        }
    }
}
