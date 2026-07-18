package com.example.citygrid.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.citygrid.data.SessionManager
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.navigation.Screen
import com.example.citygrid.ui.components.AlertItemCard
import com.example.citygrid.ui.components.SemiCircleChart
import com.example.citygrid.ui.components.StatusBadge
import com.example.citygrid.ui.components.bounceClick
import com.example.citygrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Módulo 2 — Dashboard General v2.
 *
 * Resumen del estado de los sistemas (residuos, agua, alumbrado) + alertas activas.
 * Los datos se obtienen directamente desde MqttManager flows expuestos por DashboardViewModel.
 * Incluye animaciones de entrada escalonadas (fadeIn + slideInVertically).
 */
@Composable
fun DashboardScreen(
    navController: NavController,
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val nombreUsuario = sessionManager.getNombre()

    // Leer directamente de los flows expuestos por el ViewModel (sin duplicación)
    val residuosState by viewModel.residuosState.collectAsState()
    val aguaState by viewModel.aguaState.collectAsState()
    val alumbradoState by viewModel.alumbradoState.collectAsState()
    val alertas by viewModel.alertas.collectAsState()
    val esp32Conectado by viewModel.esp32Conectado.collectAsState()

    // Calcular valores derivados
    val maxContenedor = residuosState.contenedores.maxByOrNull { it.porcentaje }
    val residuoNombre = maxContenedor?.nombre ?: "--"
    val residuoPorcentaje = maxContenedor?.porcentaje ?: 0
    val nivelAgua = aguaState.nivelTanque
    val bombaActiva = aguaState.bombaActiva
    val alumbradoOn = alumbradoState.estadoOn
    val ldrLux = alumbradoState.ldrLux

    // Usar el timestamp más reciente de todos los subsistemas
    val ultimaActualizacion = maxOf(
        residuosState.ultimoMensajeTimestamp,
        aguaState.ultimaActualizacion,
        alumbradoState.ultimaActualizacion
    )

    val tiempoTexto = remember(ultimaActualizacion) {
        if (ultimaActualizacion == 0L) "Cargando..."
        else {
            val minutos = (System.currentTimeMillis() - ultimaActualizacion) / 60000
            when {
                minutos < 1  -> "Recién actualizado"
                minutos == 1L -> "Última actualización hace 1 minuto"
                else          -> "Última actualización hace $minutos minutos"
            }
        }
    }

    val alertasActivas   = alertas.count { !it.atendida }
    val alertasCriticas  = alertas.count { it.tipo == TipoAlerta.CRITICO && !it.atendida }
    val alertasAdvert    = alertas.count { it.tipo == TipoAlerta.ADVERTENCIA && !it.atendida }
    val alertasRecientes = remember(alertas) {
        alertas.sortedByDescending { it.timestamp }.take(5)
    }

    val fechaActual = remember {
        SimpleDateFormat("dd / MM / yyyy · HH:mm", Locale.getDefault())
            .format(Date()) + " hrs"
    }

    // ── Controlar visibilidad de entrada (stagger animation) ──────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Saludo ──────────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Bienvenido",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Hola, $nombreUsuario",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Dashboard General · CityGrid",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Banner estado general ───────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = 80)) + slideInVertically(tween(400, delayMillis = 80)) { it / 3 }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = CityGridPrimaryDark.copy(alpha = 0.3f),
                                spotColor = CityGridPrimaryDark.copy(alpha = 0.4f)
                            )
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
                            Text(
                                text = "Estado General del Sistema",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFCBD5E0),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = fechaActual,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(12.dp))
                            // Indicador conectividad
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (esp32Conectado) StatusGreen else StatusRed,
                                                CircleShape
                                            )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (esp32Conectado)
                                            "ESP32 conectado"
                                        else
                                            "Sin señal — ESP32 desconectado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (esp32Conectado) StatusGreen else StatusRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (esp32Conectado && alertasActivas > 0) {
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(AmberAccent.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$alertasActivas alerta${if (alertasActivas > 1) "s" else ""} pendiente${if (alertasActivas > 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = tiempoTexto,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            }

            // ── Acceso rápido ───────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = 160)) + slideInVertically(tween(400, delayMillis = 160)) { it / 3 }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AccesoRapidoItem(
                            icon = Icons.Filled.Delete,
                            label = "Residuos",
                            iconColor = CityGridPrimary,
                            backgroundColor = CityGridPrimaryLight,
                            onClick = { navController.navigate(Screen.Residuos.route) }
                        )
                        AccesoRapidoItem(
                            icon = Icons.Filled.WaterDrop,
                            label = "Agua",
                            iconColor = CityGridPrimary,
                            backgroundColor = CityGridPrimaryLight,
                            onClick = { navController.navigate(Screen.Agua.route) }
                        )
                        AccesoRapidoItem(
                            icon = Icons.Filled.WbSunny,
                            label = "Alumbrado",
                            iconColor = AmberAccent,
                            backgroundColor = AmberLight,
                            onClick = { navController.navigate(Screen.Alumbrado.route) }
                        )
                        AccesoRapidoItem(
                            icon = Icons.Filled.Build,
                            label = "Manten.",
                            iconColor = TextSecondary,
                            backgroundColor = SurfaceContainer,
                            onClick = { navController.navigate(Screen.Mantenimiento.route) }
                        )
                    }
                }
            }

            // ── Tarjeta Nivel de Residuos ───────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(420, delayMillis = 240)) + slideInVertically(tween(420, delayMillis = 240)) { it / 3 }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .border(1.dp, DividerColor, RoundedCornerShape(24.dp))
                            .clickable { navController.navigate(Screen.Residuos.route) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nivel de Residuos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            SemiCircleChart(
                                porcentaje = residuoPorcentaje,
                                label = residuoNombre,
                                color = CityGridPrimary
                            )
                            Spacer(Modifier.height(10.dp))
                            val estadoResiduo = when {
                                residuoPorcentaje > 85 -> "LLENO"
                                residuoPorcentaje >= 50 -> "MEDIO"
                                else -> "VACÍO"
                            }
                            val textoAccionResiduo = when {
                                residuoPorcentaje > 85 -> "Requiere vaciado"
                                residuoPorcentaje >= 50 -> "Nivel estable"
                                else -> "Nivel óptimo"
                            }
                            val colorAccionResiduo = when {
                                residuoPorcentaje > 85 -> StatusRed
                                residuoPorcentaje >= 50 -> StatusYellow
                                else -> StatusGreen
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.weight(1f))
                                StatusBadge(estadoResiduo)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = textoAccionResiduo,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorAccionResiduo
                                )
                            }
                        }
                    }
                }
            }

            // ── Sistemas monitoreados ───────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(420, delayMillis = 320)) + slideInVertically(tween(420, delayMillis = 320)) { it / 3 }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Sistemas monitoreados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Tarjeta Agua
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, DividerColor, RoundedCornerShape(24.dp))
                                    .clickable { navController.navigate(Screen.Agua.route) },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(modifier = Modifier.padding(16.dp)) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(CityGridPrimaryLight, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.WaterDrop,
                                                contentDescription = null,
                                                tint = CityGridPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = "Agua",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "$nivelAgua%",
                                            color = CityGridPrimary,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp
                                        )
                                        Text(
                                            text = "Nivel del tanque",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "Bomba",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary
                                            )
                                            Spacer(Modifier.weight(1f))
                                            Text(
                                                text = if (bombaActiva) "Activa" else "Apagada",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (bombaActiva) StatusGreen else TextSecondary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    StatusBadge(
                                        estado = "NORMAL",
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    )
                                }
                            }
                            // Tarjeta Alumbrado
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, DividerColor, RoundedCornerShape(24.dp))
                                    .clickable { navController.navigate(Screen.Alumbrado.route) },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(modifier = Modifier.padding(16.dp)) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(AmberLight, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.WbSunny,
                                                contentDescription = null,
                                                tint = AmberAccent,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = "Alumbrado",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = if (alumbradoOn) "ON" else "OFF",
                                            color = if (alumbradoOn) AmberAccent else TextSecondary,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp
                                        )
                                        Text(
                                            text = "Modo automático",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "Sensor LDR",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary
                                            )
                                            Spacer(Modifier.weight(1f))
                                            Text(
                                                text = "$ldrLux lux",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AmberAccent,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    StatusBadge(
                                        estado = "AUTO",
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Alertas activas ─────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(420, delayMillis = 400)) + slideInVertically(tween(420, delayMillis = 400)) { it / 3 }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .border(1.dp, DividerColor, RoundedCornerShape(24.dp))
                            .clickable { navController.navigate(Screen.Alertas.route) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(AmberAccent.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = AmberAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Alertas activas",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Requieren atención inmediata",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                StatusBadge(estado = "$alertasActivas activas")
                            }
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (alertasCriticas > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(StatusRed, CircleShape)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "$alertasCriticas Crítica${if (alertasCriticas > 1) "s" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StatusRed,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                if (alertasAdvert > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(AmberAccent, CircleShape)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "$alertasAdvert Advertencia${if (alertasAdvert > 1) "s" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AmberAccent,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Encabezado Alertas recientes ────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = 480))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alertas recientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Historial ›",
                            style = MaterialTheme.typography.labelMedium,
                            color = CityGridPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Alertas.route)
                            }
                        )
                    }
                }
            }

            // ── Lista de alertas recientes ──────────────────────────────
            if (alertasRecientes.isEmpty()) {
                item {
                    com.example.citygrid.ui.components.EmptyState(
                        icon = Icons.Default.Check,
                        titulo = "Todo en orden",
                        subtitulo = "No hay alertas recientes — el sistema funciona correctamente"
                    )
                }
            } else {
                items(alertasRecientes) { alerta ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400, delayMillis = 560)) + slideInVertically(tween(400, delayMillis = 560)) { it / 4 }
                    ) {
                        AlertItemCard(
                            alerta = alerta,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Helper: ícono de acceso rápido ────────────────────────────────────────────
@Composable
private fun AccesoRapidoItem(
    icon: ImageVector,
    label: String,
    iconColor: Color = CityGridPrimary,
    backgroundColor: Color = CityGridPrimaryLight,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .bounceClick()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = iconColor.copy(alpha = 0.2f),
                    spotColor = iconColor.copy(alpha = 0.2f)
                )
                .background(backgroundColor, RoundedCornerShape(24.dp))
                .border(1.dp, DividerColor, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}
