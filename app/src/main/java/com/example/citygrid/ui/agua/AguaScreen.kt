package com.example.citygrid.ui.agua

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.citygrid.model.db.DbLecturaAgua
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import com.example.citygrid.ui.theme.CityGridPrimary
import com.example.citygrid.ui.theme.CityGridPrimaryDark
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.StatusYellow
import com.example.citygrid.ui.theme.AmberAccent
import com.example.citygrid.ui.theme.AmberLight
import com.example.citygrid.ui.theme.BackgroundLight
import com.example.citygrid.ui.theme.SurfaceCard
import com.example.citygrid.ui.theme.SurfaceElevated
import com.example.citygrid.ui.theme.DividerColor
import com.example.citygrid.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AguaScreen(viewModel: AguaViewModel = viewModel()) {
    val state by viewModel.aguaState.collectAsState()
    val historial by viewModel.historial.collectAsState()
    var mostrarHistorial by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = BackgroundLight,
        contentWindowInsets = WindowInsets(0.dp),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Título
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 }
            ) {
                Text(
                    text = "Gestión de Agua",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Banner estado
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 80)) + slideInVertically(tween(400, delayMillis = 80)) { it / 3 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CityGridPrimaryDark, Color(0xFF0A3D62), Color(0xFF0D5A8C))
                            ),
                            RoundedCornerShape(22.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                formatTimestamp(state.ultimaActualizacion),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                            com.example.citygrid.ui.components.StatusBadge(
                                estado = if (state.conectado) "OPERANDO" else "DESCONECTADO"
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (state.conectado) StatusGreen else StatusRed,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.conectado) "Sistema hidráulico activo" else "Sistema inactivo",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Tarjeta nivel del tanque
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 160)) + slideInVertically(tween(400, delayMillis = 160)) { it / 3 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "NIVEL DEL TANQUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${state.nivelTanque}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = CityGridPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = CityGridPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            LinearProgressIndicator(
                                progress = { state.nivelTanque / 100f },
                                modifier = Modifier.fillMaxWidth().height(12.dp),
                                color = CityGridPrimary,
                                trackColor = DividerColor,
                                strokeCap = StrokeCap.Round
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Estado bomba",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    if (state.bombaActiva) "Operando" else "Apagada",
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.bombaActiva) StatusGreen else TextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Estado general",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    state.estadoGeneral,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Sensor",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    "Ultrasónico",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Tarjeta visual nivel
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 240)) + slideInVertically(tween(400, delayMillis = 240)) { it / 3 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "NIVEL VISUAL DEL TANQUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val labelNivel = when {
                            state.nivelTanque >= 50 -> "Nivel adecuado"
                            state.nivelTanque >= 20 -> "Nivel bajo"
                            else -> "Nivel crítico"
                        }
                        SemiCircleChartAgua(
                            porcentaje = state.nivelTanque,
                            label = labelNivel
                        )
                    }
                }
            }

            // Tarjeta sistemas monitoreados
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 320)) + slideInVertically(tween(400, delayMillis = 320)) { it / 3 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "SISTEMAS MONITOREADOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        DetailRowAgua(Icons.Default.WaterDrop, "Nivel del tanque", "${state.nivelTanque}%")
                        DetailRowAgua(Icons.Default.PowerSettingsNew, "Bomba de agua", if (state.bombaActiva) "Operando" else "Apagada")
                        DetailRowAgua(Icons.Default.CheckCircle, "Estado general", state.estadoGeneral)
                        DetailRowAgua(Icons.Default.Schedule, "Última actualización", formatTimestamp(state.ultimaActualizacion))
                    }
                }
            }

            // Control Manual de Bomba
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 400)) + slideInVertically(tween(400, delayMillis = 400)) { it / 3 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.bombaActiva)
                            StatusGreen.copy(alpha = 0.06f)
                        else
                            SurfaceElevated
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "CONTROL MANUAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Bomba de Agua",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (state.bombaActiva) "Activa — modo manual (60s)" else "Apagada",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (state.bombaActiva) StatusGreen else TextSecondary
                                )
                            }
                            Switch(
                                checked = state.bombaActiva,
                                onCheckedChange = { viewModel.activarBombaManual(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CityGridPrimary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = DividerColor,
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "⚠ El ESP32 vuelve al modo automático después de 60 seg.",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberAccent
                        )
                    }
                }
            }

            // Eventos recientes
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 480))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Eventos recientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { mostrarHistorial = true }) {
                        Text(
                            "Historial ›",
                            color = CityGridPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 540))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (historial.isEmpty()) {
                        Text(
                            "Cargando eventos...",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        historial.take(3).forEach { lectura -> TarjetaEventoMiniAgua(lectura) }
                    }
                }
            }

            Text(
                "Última actualización de tabla: ${formatTimestamp(System.currentTimeMillis())}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(110.dp))
        }

        if (mostrarHistorial) {
            ModalBottomSheet(
                onDismissRequest = { mostrarHistorial = false },
                containerColor = Color.White,
                modifier = Modifier.fillMaxHeight(0.85f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        "Historial de Nivel de Agua",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(historial) { lectura -> TarjetaHistorialAgua(lectura) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun SemiCircleChartAgua(
    porcentaje: Int,
    modifier: Modifier = Modifier,
    label: String = ""
) {
    val safeProgress = porcentaje.coerceIn(0, 100)
    val color = when {
        safeProgress >= 50 -> CityGridPrimary
        safeProgress >= 20 -> StatusYellow
        else               -> StatusRed
    }

    val animatedSweep by animateFloatAsState(
        targetValue = (safeProgress / 100f) * 180f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "aguaChartSweep"
    )

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .width(190.dp)
            .height(95.dp)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 28.dp.toPx()
            val glowWidth   = 36.dp.toPx()
            val radius      = (size.width / 2f) - (strokeWidth / 2f)
            val topLeft     = Offset(strokeWidth / 2f, size.height - radius - (strokeWidth / 2f))
            val arcSize     = Size(radius * 2f, radius * 2f)

            // Track
            drawArc(
                color = DividerColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
            // Glow
            if (animatedSweep > 0f) {
                drawArc(
                    color = color.copy(alpha = 0.15f),
                    startAngle = 180f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = glowWidth, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }
            // Progreso
            if (animatedSweep > 0f) {
                drawArc(
                    color = color,
                    startAngle = 180f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "$safeProgress%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun DetailRowAgua(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = CityGridPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), color = TextSecondary)
        Text(value, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun TarjetaEventoMiniAgua(lectura: DbLecturaAgua) {
    val nivel = lectura.nivelAgua.toInt()
    val (colorIndicador, textoEvento) = when {
        nivel >= 50 -> Pair(CityGridPrimary, "Nivel de agua estable registrado")
        nivel >= 20 -> Pair(StatusYellow,    "Nivel de agua bajo (Precaución)")
        else        -> Pair(StatusRed,       "Nivel de agua crítico registrado")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colorIndicador, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    textoEvento,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                formatearFechaBd(lectura.fechaHora),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun TarjetaHistorialAgua(lectura: DbLecturaAgua) {
    val nivel = lectura.nivelAgua.toInt()
    val colorIndicador = when {
        nivel >= 50 -> CityGridPrimary
        nivel >= 20 -> StatusYellow
        else        -> StatusRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Lectura Sensor Ultrasónico",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    formatearFechaBdCompleta(lectura.fechaHora),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$nivel%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorIndicador
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = colorIndicador,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "Sin datos"
    return java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
}
private fun formatearFechaBd(fechaStr: String?): String = try { OffsetDateTime.parse(fechaStr).format(DateTimeFormatter.ofPattern("HH:mm")) } catch (e: Exception) { "--:--" }
private fun formatearFechaBdCompleta(fechaStr: String?): String = try { OffsetDateTime.parse(fechaStr).format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")) } catch (e: Exception) { fechaStr ?: "" }