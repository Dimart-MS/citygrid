package com.example.citygrid.ui.residuos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.ContenedorData
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.ui.components.BarraProgresoPersonalizada
import com.example.citygrid.ui.components.ElementoEventoLista
import com.example.citygrid.ui.components.IconoPersonalizado
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import kotlinx.coroutines.delay

@Composable
fun ResiduosScreen(
    viewModel: ResiduosViewModel = viewModel(),
    onNavigateToAlerts: () -> Unit = {}
) {
    val state by viewModel.residuosState.collectAsState()
    val maxContenedor by viewModel.maxContenedor.collectAsState()
    val criticalCount by viewModel.criticalContainersCount.collectAsState()
    val eventosRecientes by viewModel.eventosRecientes.collectAsState()
    
    val scrollState = rememberScrollState()

    // Ticker para refrescar dinámicamente los segundos de inactividad
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.conectado, state.ultimoMensajeTimestamp) {
        if (!state.conectado) {
            while (true) {
                currentTime = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    val segundosInactivo = ((currentTime - state.ultimoMensajeTimestamp) / 1000).coerceAtLeast(0)

    // Cargar datos al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarAlertas()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Gestión de Residuos — CityGrid",
                    color = Color(0xFF2D3748),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Banner superior de estado reactivo
                BannerEstadoResiduos(criticalCount = criticalCount)
                
                // Alerta de Inactividad (ESP32 fuera de línea)
                if (!state.conectado) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE53E3E), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alerta de Inactividad",
                                tint = Color(0xFFE53E3E),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Dispositivo Inactivo",
                                    color = Color(0xFFC53030),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "No se han recibido lecturas del ESP32 hace $segundosInactivo segundos.",
                                    color = Color(0xFF742A2A),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                
                // Medidor semicircular del contenedor más lleno
                TarjetaMedidor(maxContenedor = maxContenedor)
                
                Text(
                    text = "CONTENEDORES MONITOREADOS",
                    color = Color(0xFF718096),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                // Listar contenedores dinámicamente desde el StateFlow
                state.contenedores.forEach { contenedor ->
                    TarjetaContenedor(contenedor = contenedor)
                }
                
                // Historial corto de eventos recientes
                TarjetaEventosRecientesResiduos(
                    eventos = eventosRecientes,
                    onVerAlertas = onNavigateToAlerts
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val timeString = remember(state.contenedores) {
                    val maxTimestamp = state.contenedores.maxOfOrNull { it.ultimaActualizacion } ?: System.currentTimeMillis()
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy · hh:mm a", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(maxTimestamp))
                }
                Text(
                    text = "Última actualización: $timeString",
                    color = Color(0xFF718096),
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun BannerEstadoResiduos(criticalCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F3540))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val dateString = remember {
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm 'hrs'", java.util.Locale.getDefault())
                    sdf.format(java.util.Date())
                }
                Text(
                    text = dateString,
                    color = Color(0xFFCBD5E0),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                if (criticalCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFFAF0), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFDD6B20), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚠ $criticalCount contenedor crítico" + if (criticalCount > 1) "s" else "",
                            color = Color(0xFFD69E2E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE6F7F0), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF00A896), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "✓ Sistema OK",
                            color = Color(0xFF00A896),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF00A896), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sistema de residuos activo",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TarjetaMedidor(maxContenedor: ContenedorData?) {
    val porcentaje = maxContenedor?.porcentaje ?: 0
    val nombre = maxContenedor?.nombre ?: "Sin Datos"
    
    val status = when {
        porcentaje > 85 -> "Lleno — Requiere vaciado"
        porcentaje >= 50 -> "Medio — Nivel estable"
        else -> "Vacío — Nivel óptimo"
    }

    val (_, _, progressColor) = obtenerColoresContenedor(
        when {
            porcentaje > 85 -> "Lleno"
            porcentaje >= 50 -> "Medio"
            else -> "Vacío"
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nivel de Residuos — Contenedor más lleno",
                color = Color(0xFF2D3748),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                DibujoMedidorSegmentado(porcentaje = porcentaje, colorActivo = progressColor)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "$porcentaje%", color = Color(0xFF2D3748), fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    Text(text = "Contenedor de $nombre", color = Color(0xFF718096), fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = status,
                    color = progressColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun DibujoMedidorSegmentado(porcentaje: Int, colorActivo: Color) {
    Canvas(
        modifier = Modifier
            .width(180.dp)
            .height(90.dp)
    ) {
        val w = size.width
        val h = size.height
        val numSegments = 10
        val startAngle = 180f
        val totalSweep = 180f
        val segmentSweep = (totalSweep / numSegments) * 0.8f
        val gapSweep = (totalSweep / numSegments) * 0.2f
        val strokeWidthPx = 14.dp.toPx()

        // Determinar cuántos segmentos pintar según el porcentaje actual
        val activeSegments = (porcentaje / 10).coerceIn(0, 10)

        for (i in 0 until numSegments) {
            val segStart = startAngle + i * (segmentSweep + gapSweep)
            val color = if (i < activeSegments) colorActivo else Color(0xFFE2E8F0)
            drawArc(
                color = color,
                startAngle = segStart,
                sweepAngle = segmentSweep,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                size = size.copy(width = w - strokeWidthPx, height = (h * 2) - strokeWidthPx),
                topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
            )
        }
    }
}

@Composable
fun TarjetaContenedor(contenedor: ContenedorData) {
    val status = when {
        contenedor.porcentaje > 85 -> "Lleno"
        contenedor.porcentaje >= 50 -> "Medio"
        else -> "Vacío"
    }

    val icon = when (contenedor.tipo.lowercase()) {
        "plastico" -> "plastic"
        "inorganico" -> "trash"
        "organico" -> "leaf"
        else -> "trash"
    }

    val (statusColor, statusBg, progressColor) = obtenerColoresContenedor(status)
    val nameString = when (contenedor.tipo.lowercase()) {
        "plastico" -> "Plástico"
        "inorganico" -> "Inorgánico"
        "organico" -> "Orgánico"
        else -> contenedor.nombre
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(statusBg.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconoPersonalizado(name = icon, tint = progressColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = nameString, color = Color(0xFF2D3748), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF2D3748), fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                        append("${contenedor.porcentaje}")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF718096), fontSize = 13.sp)) {
                        append(" % de capacidad")
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            BarraProgresoPersonalizada(progress = contenedor.porcentaje / 100f, color = progressColor)
            Spacer(modifier = Modifier.height(10.dp))
            
            val updateTime = remember(contenedor.ultimaActualizacion) {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy · hh:mm a", java.util.Locale.getDefault())
                sdf.format(java.util.Date(contenedor.ultimaActualizacion))
            }
            Text(text = "Última actualización: $updateTime", color = Color(0xFFA0AEC0), fontSize = 10.sp)
        }
    }
}

private fun obtenerColoresContenedor(status: String): Triple<Color, Color, Color> {
    return when (status) {
        "Lleno" -> Triple(Color(0xFFC53030), Color(0xFFFFF5F5), Color(0xFFE53E3E))
        "Medio" -> Triple(Color(0xFFD69E2E), Color(0xFFFFFAF0), Color(0xFFECC94B))
        else -> Triple(Color(0xFF00A896), Color(0xFFE6F7F0), Color(0xFF00A896))
    }
}

@Composable
fun TarjetaEventosRecientesResiduos(
    eventos: List<Alerta>,
    onVerAlertas: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Eventos recientes",
                    color = Color(0xFF2D3748),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Historial ›",
                    color = Color(0xFF00A8CC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onVerAlertas() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (eventos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay eventos recientes",
                        color = Color(0xFF718096),
                        fontSize = 13.sp
                    )
                }
            } else {
                eventos.forEachIndexed { index, evento ->
                    val timeString = remember(evento.timestamp) {
                        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(evento.timestamp))
                    }
                    val dotColor = when (evento.tipo) {
                        TipoAlerta.CRITICO -> Color(0xFFE53E3E)
                        TipoAlerta.ADVERTENCIA -> Color(0xFFECC94B)
                        TipoAlerta.INFORMACION -> Color(0xFF00A8CC)
                        else -> Color(0xFF00A896)
                    }

                    ElementoEventoLista(
                        title = evento.titulo,
                        subtitle = "${evento.tipo.name} · $timeString",
                        dotColor = dotColor
                    )
                    
                    if (index < eventos.size - 1) {
                        HorizontalDivider(color = Color(0xFFEDF2F7), modifier = Modifier.padding(vertical = 10.dp))
                    }
                }
            }
        }
    }
}