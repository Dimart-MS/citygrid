package com.example.citygrid.ui.residuos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.example.citygrid.ui.components.TarjetaAdvertenciaConectividad
import com.example.citygrid.ui.components.bounceClick
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.citygrid.ui.theme.*
import com.example.citygrid.utils.formatTimestamp
import com.example.citygrid.utils.rememberElapsedSeconds
import com.example.citygrid.utils.Constants

@Composable
fun ResiduosScreen(
    viewModel: ResiduosViewModel = viewModel(),
    onNavigateToAlerts: () -> Unit = {}
) {
    val state by viewModel.residuosState.collectAsState()
    val maxContenedor by viewModel.maxContenedor.collectAsState()
    val criticalCount by viewModel.criticalContainersCount.collectAsState()
    val eventosRecientes by viewModel.eventosRecientes.collectAsState()

    // Usar utilidad reactiva para calcular segundos inactivo (sin bucle while)
    val segundosInactivo = rememberElapsedSeconds(state.ultimoMensajeTimestamp)

    LaunchedEffect(Unit) {
        viewModel.cargarAlertas()
    }

    // Stagger animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Título
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 }
                ) {
                    Text(
                        text = "Gestión de Residuos",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Banner estado
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = 80)) + slideInVertically(tween(400, delayMillis = 80)) { it / 3 }
                ) {
                    BannerEstadoResiduos(
                        criticalCount = criticalCount,
                        conectado = state.conectado,
                        contenedores = state.contenedores
                    )
                }
            }

            // Alerta de Inactividad
            if (!state.conectado) {
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(300, delayMillis = 120))
                    ) {
                        TarjetaAdvertenciaConectividad(segundosInactivo = segundosInactivo)
                    }
                }
            }

            // Medidor
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(420, delayMillis = 160)) + slideInVertically(tween(420, delayMillis = 160)) { it / 3 }
                ) {
                    TarjetaMedidor(maxContenedor = maxContenedor)
                }
            }

            // Header contenedores
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(300, delayMillis = 240))
                ) {
                    Text(
                        text = "CONTENEDORES MONITOREADOS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Lista de contenedores con stagger limitado a primeros 5 elementos
            itemsIndexed(state.contenedores.take(5)) { index, contenedor ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(420, delayMillis = 300 + index * 80)) +
                            slideInVertically(tween(420, delayMillis = 300 + index * 80)) { it / 3 }
                ) {
                    TarjetaContenedor(contenedor = contenedor)
                }
            }

            // Si hay más de 5 contenedores, mostrar indicador
            if (state.contenedores.size > 5) {
                item {
                    Text(
                        text = "+ ${state.contenedores.size - 5} contenedores más",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Eventos recientes
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(420, delayMillis = 480)) + slideInVertically(tween(420, delayMillis = 480)) { it / 3 }
                ) {
                    TarjetaEventosRecientesResiduos(
                        eventos = eventosRecientes,
                        onVerAlertas = onNavigateToAlerts
                    )
                }
            }

            // Footer con timestamp
            item {
                val timeString = remember(state.contenedores) {
                    formatTimestamp(
                        state.contenedores.maxOfOrNull { it.ultimaActualizacion } ?: System.currentTimeMillis(),
                        "dd/MM/yyyy · hh:mm a"
                    )
                }
                Text(
                    text = "Última actualización: $timeString",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BannerEstadoResiduos(criticalCount: Int, conectado: Boolean, contenedores: List<ContenedorData>) {
    val algunDesconectado = contenedores.any { !it.activo }

    val (badgeText, badgeColor, statusText) = when {
        !conectado -> Triple("⚠ DESCONECTADO", StatusRed, "Dispositivo central fuera de línea")
        algunDesconectado -> Triple("⚠ SENSOR ERROR", AmberAccent, "Fallo de hardware detectado")
        criticalCount > 0 -> Triple("⚠ $criticalCount CRÍTICO" + if (criticalCount > 1) "s" else "", AmberAccent, "Atención requerida — Contenedor crítico")
        else -> Triple("✓ SISTEMA OK", StatusGreen, "Monitoreo activo — Todo en orden")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(CityGridPrimaryDark, Color(0xFF0A3D62), Color(0xFF0D5A8C))
                ),
                RoundedCornerShape(24.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatTimestamp(System.currentTimeMillis()),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(badgeColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun TarjetaMedidor(maxContenedor: ContenedorData?) {
    val porcentaje = maxContenedor?.porcentaje ?: 0
    val nombre = maxContenedor?.nombre ?: "Sin Datos"
    val activo = maxContenedor?.activo ?: true

    val status = if (activo) {
        when {
            porcentaje > Constants.UMBRAL_RESIDUOS_CRITICO -> "Lleno — Requiere vaciado"
            porcentaje >= Constants.UMBRAL_RESIDUOS_MEDIO -> "Medio — Nivel estable"
            else -> "Vacío — Nivel óptimo"
        }
    } else {
        "Sensor Desconectado"
    }

    val (_, _, progressColor) = obtenerColoresContenedor(
        if (activo) {
            when {
                porcentaje > Constants.UMBRAL_RESIDUOS_CRITICO -> "Lleno"
                porcentaje >= Constants.UMBRAL_RESIDUOS_MEDIO -> "Medio"
                else -> "Vacío"
            }
        } else {
            "Desconectado"
        }
    )

    val animatedPorcentaje by animateFloatAsState(
        targetValue = porcentaje.toFloat(),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "medidorPorcentaje"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DividerColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nivel de Residuos — Contenedor más lleno",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
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
                DibujoMedidorSegmentado(porcentaje = animatedPorcentaje, colorActivo = progressColor)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${animatedPorcentaje.toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                    Text(
                        text = "Contenedor de $nombre",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(progressColor, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = status,
                    color = progressColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun DibujoMedidorSegmentado(porcentaje: Float, colorActivo: Color) {
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
        val segmentSweep = (totalSweep / numSegments) * 0.80f
        val gapSweep = (totalSweep / numSegments) * 0.20f
        val strokeWidthPx = 16.dp.toPx()

        val activeSegments = (porcentaje / 10f).coerceIn(0f, 10f)

        for (i in 0 until numSegments) {
            val segStart = startAngle + i * (segmentSweep + gapSweep)
            val color = when {
                i < activeSegments.toInt() -> colorActivo
                i == activeSegments.toInt() && activeSegments % 1f > 0f -> {
                    val fraction = activeSegments % 1f
                    colorActivo.copy(alpha = fraction)
                }
                else -> DividerColor.copy(alpha = 0.6f)
            }
            // Glow para segmentos activos
            if (i < activeSegments.toInt()) {
                drawArc(
                    color = colorActivo.copy(alpha = 0.15f),
                    startAngle = segStart,
                    sweepAngle = segmentSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx + 4.dp.toPx(), cap = StrokeCap.Round),
                    size = size.copy(width = w - strokeWidthPx, height = (h * 2) - strokeWidthPx),
                    topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
                )
            }
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
    val status = if (contenedor.activo) {
        when {
            contenedor.porcentaje > Constants.UMBRAL_RESIDUOS_CRITICO -> "Lleno"
            contenedor.porcentaje >= Constants.UMBRAL_RESIDUOS_MEDIO -> "Medio"
            else -> "Vacío"
        }
    } else {
        "Desconectado"
    }

    val icon = when (contenedor.tipo.lowercase()) {
        "plastico"   -> "plastic"
        "inorganico" -> "trash"
        "organico"   -> "leaf"
        else         -> "trash"
    }

    val (_, _, progressColor) = obtenerColoresContenedor(status)
    val nameString = when (contenedor.tipo.lowercase()) {
        "plastico"   -> "Plástico"
        "inorganico" -> "Inorgánico"
        "organico"   -> "Orgánico"
        else         -> contenedor.nombre
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DividerColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Header de color semántico
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(progressColor.copy(alpha = 0.12f), Color.Transparent)
                    ),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(progressColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconoPersonalizado(name = icon, tint = progressColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = nameString,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                com.example.citygrid.ui.components.StatusBadge(estado = status)
            }
        }

        HorizontalDivider(color = DividerColor)

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = progressColor, fontWeight = FontWeight.Bold)) {
                            append("${contenedor.porcentaje}")
                        }
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append(" % de capacidad")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Libre: ${100 - contenedor.porcentaje}%",
                    color = if (contenedor.porcentaje > Constants.UMBRAL_RESIDUOS_CRITICO) StatusRed else TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            BarraProgresoPersonalizada(progress = contenedor.porcentaje / 100f, color = progressColor)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Última actualización: ${formatTimestamp(contenedor.ultimaActualizacion, "dd/MM/yyyy · hh:mm a")}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

private fun obtenerColoresContenedor(status: String): Triple<Color, Color, Color> {
    return when (status) {
        "Lleno"        -> Triple(StatusRed,    StatusRed.copy(alpha = 0.12f),    StatusRed)
        "Medio"        -> Triple(StatusYellow, StatusYellow.copy(alpha = 0.15f), StatusYellow)
        "Desconectado" -> Triple(Color.Gray,   Color.Gray.copy(alpha = 0.12f),   Color.Gray)
        else           -> Triple(StatusGreen,  StatusGreen.copy(alpha = 0.12f),  StatusGreen)
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
            .border(1.dp, DividerColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Eventos recientes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Historial ›",
                    color = CityGridPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                eventos.forEachIndexed { index, evento ->
                    val timeString = formatTimestamp(evento.timestamp, "hh:mm a")
                    val dotColor = when (evento.tipo) {
                        TipoAlerta.CRITICO     -> StatusRed
                        TipoAlerta.ADVERTENCIA -> AmberAccent
                        TipoAlerta.INFORMACION -> StatusBlue
                        else                   -> StatusGreen
                    }
                    ElementoEventoLista(
                        title = evento.titulo,
                        subtitle = "${evento.tipo.name} · $timeString",
                        dotColor = dotColor
                    )
                    if (index < eventos.size - 1) {
                        HorizontalDivider(
                            color = DividerColor,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
