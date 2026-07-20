package com.example.citygrid.ui.alumbrado

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.citygrid.model.db.DbLecturaLuminaria
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import com.example.citygrid.ui.components.TarjetaAdvertenciaConectividad
import com.example.citygrid.ui.components.bounceClick

import com.example.citygrid.ui.theme.CityGridPrimary
import com.example.citygrid.ui.theme.CityGridPrimaryDark
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.AmberAccent
import com.example.citygrid.ui.theme.AmberLight
import com.example.citygrid.ui.theme.AmberDark
import com.example.citygrid.ui.theme.SurfaceCard
import com.example.citygrid.ui.theme.DividerColor
import com.example.citygrid.ui.theme.TextSecondary
import com.example.citygrid.ui.theme.brandGradient
import com.example.citygrid.ui.theme.BorderWidth
import com.example.citygrid.ui.theme.Radius
import com.example.citygrid.ui.theme.Spacing
import com.example.citygrid.ui.theme.Elevation
import com.example.citygrid.utils.formatTimestamp
import com.example.citygrid.utils.rememberElapsedSeconds
import com.example.citygrid.utils.Constants

// NOTA: Las val ColorPrincipal/ColorNocheLuna/etc. han sido eliminadas de este archivo.
// Se reemplazaron por referencias directas a Color.kt para evitar conflicto de nombres
// con AguaScreen.kt que declaraba los mismos identifiers en el mismo paquete.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumbradoScreen(viewModel: AlumbradoViewModel = viewModel()) {
    val state by viewModel.alumbradoState.collectAsState()
    val historial by viewModel.historial.collectAsState()
    val enviandoComando by viewModel.enviandoComando.collectAsState()
    var mostrarHistorial by remember { mutableStateOf(false) }

    // Usar utilidad reactiva para calcular segundos inactivo (sin bucle while)
    val segundosInactivo = rememberElapsedSeconds(state.ultimaActualizacion)

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
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
                    text = "Alumbrado Inteligente",
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
                            Brush.linearGradient(colors = brandGradient()),
                            RoundedCornerShape(Radius.lg)
                        )
                        .padding(Spacing.xl)
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
                                        if (state.conectado) {
                                            if (state.estadoOn) AmberAccent else StatusRed
                                        } else {
                                            Color.Gray
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.conectado) {
                                    if (state.estadoOn) "Sistema de alumbrado activo" else "Sistema inactivo"
                                } else {
                                    "Sensor Desconectado"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Alerta de Inactividad
            if (!state.conectado) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(300, delayMillis = 100))
                ) {
                    TarjetaAdvertenciaConectividad(segundosInactivo = segundosInactivo)
                }
            }

            // Tarjeta de Modo de Operación (AUTO / MANUAL)
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 120)) + slideInVertically(tween(400, delayMillis = 120)) { it / 3 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderWidth.thin, DividerColor, RoundedCornerShape(Radius.lg)),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.sm)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xl),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "MODO DE OPERACIÓN",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (enviandoComando) "Enviando comando..."
                                       else if (state.modo == "AUTO") "Modo Automático Activo"
                                       else "Modo Manual / Forzado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (enviandoComando) AmberAccent else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (enviandoComando) "Por favor espere..."
                                       else if (state.modo == "AUTO") "Las luces responden al sensor de luz LDR."
                                       else "El sensor LDR está desactivado.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (enviandoComando) AmberAccent else TextSecondary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (state.modo == "AUTO") "AUTO" else "MANUAL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (state.modo == "AUTO") CityGridPrimary else TextSecondary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Switch(
                                checked = (state.modo == "AUTO"),
                                onCheckedChange = { if (!enviandoComando) viewModel.cambiarModo(it) },
                                enabled = !enviandoComando,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CityGridPrimary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = DividerColor
                                ),
                                modifier = Modifier.scale(0.85f)
                            )
                        }
                    }
                }
            }

            // Tarjetas Estado + Condición
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 160)) + slideInVertically(tween(400, delayMillis = 160)) { it / 3 }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Tarjeta Estado con Switch
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = BorderWidth.thin,
                                color = if (state.estadoOn) AmberAccent.copy(alpha = 0.3f) else DividerColor,
                                shape = RoundedCornerShape(Radius.lg)
                            ),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.sm)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (state.estadoOn) AmberLight else DividerColor,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = if (state.estadoOn) AmberAccent else TextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Switch(
                                    checked = state.estadoOn,
                                    onCheckedChange = { if (!enviandoComando) viewModel.alternarLucesManual(it) },
                                    enabled = !enviandoComando,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = CityGridPrimary,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = DividerColor
                                    ),
                                    modifier = Modifier.scale(0.85f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Estado",
                                color = TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                if (state.estadoOn) "ON" else "OFF",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (state.estadoOn) AmberAccent else TextSecondary
                            )
                            Text(
                                if (state.modo == "MANUAL") "Control manual" else "Encendido automático",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // Tarjeta Condición LDR
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = BorderWidth.thin,
                                color = if (state.condicionNoche) CityGridPrimary.copy(alpha = 0.3f)
                                        else AmberAccent.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(Radius.lg)
                            ),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.condicionNoche) CityGridPrimaryDark.copy(alpha = 0.06f)
                                             else AmberLight.copy(alpha = 0.25f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.sm)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (state.condicionNoche) CityGridPrimaryDark.copy(alpha = 0.15f)
                                        else AmberLight,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (state.condicionNoche) Icons.Default.ModeNight else Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = if (state.condicionNoche) CityGridPrimary else AmberAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Spacer(modifier = Modifier.height(16.dp)) // align height with switch card
                            Text(
                                "Condición",
                                color = TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                if (state.condicionNoche) "Noche" else "Día",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (state.condicionNoche) CityGridPrimary else AmberAccent
                            )
                            Text(
                                "Detectada por LDR",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Detalles del sensor
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 240)) + slideInVertically(tween(400, delayMillis = 240)) { it / 3 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderWidth.thin, DividerColor, RoundedCornerShape(Radius.lg)),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.sm)
                ) {
                    Column(modifier = Modifier.padding(Spacing.xl), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            "DETALLES DEL SISTEMA",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        DetailRowAlumbrado(
                            Icons.Default.Sensors,
                            "Sensor LDR",
                            if (state.conectado) "${state.ldrLux} Lux" else "Desconectado"
                        )
                        DetailRowAlumbrado(Icons.Default.Lightbulb, "Luminarias activas", "${state.luminariasActivas} luminarias")
                        DetailRowAlumbrado(Icons.Default.Settings, "Modo", state.modo)
                    }
                }
            }

            // Eventos
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 320))
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
                        Text("Historial ›", color = CityGridPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 400))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    historial.take(3).forEach { lectura -> TarjetaEventoMiniAlumbrado(lectura) }
                }
            }

            Text(
                "Última actualización: ${formatTimestamp(System.currentTimeMillis())}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(110.dp))
        }

        if (mostrarHistorial) {
            ModalBottomSheet(
                onDismissRequest = { mostrarHistorial = false },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxHeight(0.85f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        "Historial Completo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(historial) { lectura -> TarjetaHistorialLDR(lectura) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DetailRowAlumbrado(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = CityGridPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, modifier = Modifier.weight(1f), color = TextSecondary)
        Text(value, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun TarjetaEventoMiniAlumbrado(lectura: DbLecturaLuminaria) {
    val esNoche = lectura.valorLdr < 100
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (esNoche) CityGridPrimaryDark.copy(alpha = 0.1f) else AmberLight,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (esNoche) Icons.Default.ModeNight else Icons.Default.WbSunny,
                    null,
                    tint = if (esNoche) CityGridPrimary else AmberAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    if (esNoche) "Lectura Nocturna" else "Lectura Diurna",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Valor: ${lectura.valorLdr} Lux | ${formatearFechaBd(lectura.fechaHora)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun TarjetaHistorialLDR(lectura: DbLecturaLuminaria) {
    val esNoche = lectura.valorLdr < 100
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
                    "Lectura del Sensor",
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
                    "${lectura.valorLdr} Lux",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (esNoche) CityGridPrimary else AmberAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (esNoche) Icons.Default.ModeNight else Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = if (esNoche) CityGridPrimary else AmberAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String =
    java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
private fun formatearFechaBd(fechaStr: String?): String =
    try { OffsetDateTime.parse(fechaStr).format(DateTimeFormatter.ofPattern("HH:mm")) } catch (e: Exception) { "--:--" }
private fun formatearFechaBdCompleta(fechaStr: String?): String =
    try { OffsetDateTime.parse(fechaStr).format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")) } catch (e: Exception) { fechaStr ?: "" }
