package com.example.citygrid.ui.agua

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.citygrid.model.db.DbLecturaAgua
import com.example.citygrid.ui.components.CityGridTopBar
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

val ColorPrincipal = Color(0xFF0FA3B1)
val ColorEstadoFondo = Color(0x402A9D8F)
val ColorPuntoActivo = Color(0xFF2EE8A5)
val ColorNocheLuna = Color(0xFF284553)
val ColorAlertaMedia = Color(0xFFFFB300)
val ColorAlertaCritica = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AguaScreen(viewModel: AguaViewModel = viewModel()) {
    val state by viewModel.aguaState.collectAsState()
    val historial by viewModel.historial.collectAsState()
    var mostrarHistorial by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        contentWindowInsets = WindowInsets(0.dp),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("Bienvenido", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = ColorPrincipal, fontWeight = FontWeight.Bold)) { append("Gestión de Agua") }
                    withStyle(style = SpanStyle(color = ColorNocheLuna, fontWeight = FontWeight.Bold)) { append(" - CityGrid") }
                }, style = MaterialTheme.typography.headlineSmall)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorNocheLuna),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(formatTimestamp(state.ultimaActualizacion), style = MaterialTheme.typography.bodySmall, color = Color.LightGray)

                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E3A47), RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    if (state.conectado) Color(0xFF00A896) else Color.Red,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (state.conectado) "✓ Operando" else "✗ Desconectado",
                                color = if (state.conectado) Color(0xFF00A896) else Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(if (state.conectado) ColorPuntoActivo else Color.Red, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.conectado) "Sistema hidráulico activo" else "Sistema inactivo",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("NIVEL DEL TANQUE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${state.nivelTanque}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ColorPrincipal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = ColorPrincipal, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        LinearProgressIndicator(
                            progress = { state.nivelTanque / 100f },
                            modifier = Modifier.fillMaxWidth().height(12.dp),
                            color = ColorPrincipal,
                            trackColor = Color(0xFFE0E0E0),
                            strokeCap = StrokeCap.Round
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Estado bomba", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(if (state.bombaActiva) "Operando" else "Apagada", fontWeight = FontWeight.Bold, color = ColorPrincipal)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Estado general", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(state.estadoGeneral, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sensor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("Ultrasónico", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NIVEL VISUAL DEL TANQUE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("SISTEMAS MONITOREADOS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                    DetailRowAgua(Icons.Default.WaterDrop, "Nivel del tanque", "${state.nivelTanque}%")
                    DetailRowAgua(Icons.Default.PowerSettingsNew, "Bomba de agua", if (state.bombaActiva) "Operando" else "Apagada")
                    DetailRowAgua(Icons.Default.CheckCircle, "Estado general", state.estadoGeneral)
                    DetailRowAgua(Icons.Default.Schedule, "Última actualización", formatTimestamp(state.ultimaActualizacion))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Eventos recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { mostrarHistorial = true }) { Text("Historial >", color = ColorPrincipal) }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (historial.isEmpty()) {
                    Text("Cargando eventos...", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                } else {
                    historial.take(3).forEach { lectura -> TarjetaEventoMiniAgua(lectura) }
                }
            }

            Text("Última actualización de tabla: ${formatTimestamp(System.currentTimeMillis())}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (mostrarHistorial) {
            ModalBottomSheet(
                onDismissRequest = { mostrarHistorial = false },
                containerColor = Color.White,
                modifier = Modifier.fillMaxHeight(0.85f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Historial de Nivel de Agua", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ColorNocheLuna)
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
        safeProgress >= 50 -> ColorPrincipal
        safeProgress >= 20 -> ColorAlertaMedia
        else -> ColorAlertaCritica
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .width(180.dp)
            .height(90.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 24.dp.toPx()
            val radius = (size.width / 2f) - (strokeWidth / 2f)
            val topLeft = Offset(
                x = strokeWidth / 2f,
                y = size.height - radius - (strokeWidth / 2f)
            )
            val arcSize = Size(radius * 2f, radius * 2f)

            drawArc(
                color = Color(0xFFE0E0E0),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            val sweep = (safeProgress / 100f) * 180f
            if (sweep > 0f) {
                drawArc(
                    color = color,
                    startAngle = 180f,
                    sweepAngle = sweep,
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
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DetailRowAgua(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ColorPrincipal, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), color = Color.DarkGray)
        Text(value, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun TarjetaEventoMiniAgua(lectura: DbLecturaAgua) {
    val nivel = lectura.nivelAgua.toInt()
    val (colorFondo, textoEvento) = when {
        nivel >= 50 -> Pair(ColorPrincipal, "Nivel de agua estable registrado")
        nivel >= 20 -> Pair(ColorAlertaMedia, "Nivel de agua bajo (Precaución)")
        else -> Pair(ColorAlertaCritica, "Nivel de agua crítico registrado")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(colorFondo, CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Text(textoEvento, fontWeight = FontWeight.Medium, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
            }
            Text(formatearFechaBd(lectura.fechaHora), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun TarjetaHistorialAgua(lectura: DbLecturaAgua) {
    val nivel = lectura.nivelAgua.toInt()
    val colorIndicador = when {
        nivel >= 50 -> ColorPrincipal
        nivel >= 20 -> ColorAlertaMedia
        else -> ColorAlertaCritica
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Lectura Sensor Ultrasónico", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(formatearFechaBdCompleta(lectura.fechaHora), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$nivel%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorIndicador)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = colorIndicador, modifier = Modifier.size(24.dp))
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String = java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
private fun formatearFechaBd(fechaStr: String?): String = try { OffsetDateTime.parse(fechaStr).format(DateTimeFormatter.ofPattern("HH:mm")) } catch (e: Exception) { "--:--" }
private fun formatearFechaBdCompleta(fechaStr: String?): String = try { OffsetDateTime.parse(fechaStr).format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")) } catch (e: Exception) { fechaStr ?: "" }