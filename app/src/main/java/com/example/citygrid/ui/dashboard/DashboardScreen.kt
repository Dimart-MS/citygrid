package com.example.citygrid.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.citygrid.ui.theme.CityGridPrimary
import com.example.citygrid.ui.theme.StatusBlue
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.StatusYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Módulo 2 — Dashboard General.
 *
 * Resumen del estado de los sistemas (residuos, agua, alumbrado) + alertas activas.
 * Los datos se obtienen desde Supabase via DashboardViewModel.
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

    val residuoNombre by viewModel.residuoNombre.collectAsState()
    val residuoPorcentaje by viewModel.residuoPorcentaje.collectAsState()
    val nivelAgua by viewModel.nivelAgua.collectAsState()
    val bombaActiva by viewModel.bombaActiva.collectAsState()
    val alumbradoOn by viewModel.alumbradoOn.collectAsState()
    val ldrLux by viewModel.ldrLux.collectAsState()
    val alertas by viewModel.alertas.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val ultimaActualizacion by viewModel.ultimaActualizacion.collectAsState()
    val esp32Conectado by viewModel.esp32Conectado.collectAsState()

    val tiempoTexto = remember(ultimaActualizacion) {
        if (ultimaActualizacion == 0L) "Cargando..."
        else {
            val minutos = (System.currentTimeMillis() - ultimaActualizacion) / 60000
            when {
                minutos < 1 -> "Recién actualizado"
                minutos == 1L -> "Última actualización hace 1 minuto"
                else -> "Última actualización hace $minutos minutos"
            }
        }
    }

    val alertasActivas = alertas.count { !it.atendida }
    val alertasCriticas = alertas.count { it.tipo == TipoAlerta.CRITICO && !it.atendida }
    val alertasAdvertencia = alertas.count { it.tipo == TipoAlerta.ADVERTENCIA && !it.atendida }
    val alertasRecientes = remember(alertas) {
        alertas.sortedByDescending { it.timestamp }.take(5)
    }

    val fechaActual = SimpleDateFormat("dd / MM / yyyy · HH:mm", Locale.getDefault())
        .format(Date()) + " hrs"

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Contenido con padding lateral ────────────────────────────
            // Nota: el header (BloqueEncabezado) lo provee el Scaffold global
            // de MainActivity. Esta pantalla no debe pintar su propia TopBar.
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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

            // ── Banner estado general ───────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF284553)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Estado General del Sistema",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = fechaActual,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E3A47), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
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
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (esp32Conectado)
                                        "ESP32 Conectado — $alertasActivas alerta(s) pendiente(s)"
                                    else
                                        "Sin señal — ESP32 desconectado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = tiempoTexto,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // ── Acceso rápido ───────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AccesoRapidoItem(
                        icon = Icons.Filled.Delete,
                        label = "Residuos",
                        onClick = { navController.navigate(Screen.Residuos.route) }
                    )
                    AccesoRapidoItem(
                        icon = Icons.Filled.WaterDrop,
                        label = "Agua",
                        onClick = { navController.navigate(Screen.Agua.route) }
                    )
                    AccesoRapidoItem(
                        icon = Icons.Filled.WbSunny,
                        label = "Alumbrado",
                        onClick = { navController.navigate(Screen.Alumbrado.route) }
                    )
                    // Boton para mantenimiento
                    AccesoRapidoItem(
                        icon = Icons.Filled.Build,
                        label = "Mantenimiento",
                        onClick = { navController.navigate(Screen.Mantenimiento.route) }
                    )
                }
            }

            // ── Tarjeta Nivel de Residuos ───────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nivel de Residuos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        SemiCircleChart(
                            porcentaje = residuoPorcentaje,
                            label = residuoNombre
                        )
                        Spacer(Modifier.height(8.dp))
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
                            StatusBadge("LLENO")
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Requiere vaciado",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusRed
                            )
                        }
                    }
                }
            }

            // ── Sistemas monitoreados ───────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Sistemas monitoreados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                Column {
                                    Icon(
                                        Icons.Filled.WaterDrop,
                                        contentDescription = null,
                                        tint = Color(0xFF26A69A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Agua",
                                        color = Color(0xFF424242),
                                        fontSize = 13.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "$nivelAgua%",
                                        color = Color(0xFF26A69A),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Nivel del tanque",
                                        color = Color(0xFF9E9E9E),
                                        fontSize = 11.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Bomba",
                                            color = Color(0xFF9E9E9E),
                                            fontSize = 12.sp
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            text = if (bombaActiva) "Activada" else "Apagada",
                                            color = Color(0xFF26A69A),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "NORMAL",
                                        color = Color(0xFF2E7D32),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                Column {
                                    Icon(
                                        Icons.Filled.WbSunny,
                                        contentDescription = null,
                                        tint = Color(0xFFFFA726),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Alumbrado",
                                        color = Color(0xFF424242),
                                        fontSize = 13.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = if (alumbradoOn) "ON" else "OFF",
                                        color = Color(0xFFFFA726),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Modo automático",
                                        color = Color(0xFF9E9E9E),
                                        fontSize = 11.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Sensor LDR",
                                            color = Color(0xFF9E9E9E),
                                            fontSize = 12.sp
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            text = "$ldrLux lux",
                                            color = Color(0xFF26A69A),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AUTO",
                                        color = Color(0xFF2E7D32),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Alertas activas ─────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(StatusYellow.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚠", color = StatusYellow)
                            }
                            Spacer(Modifier.width(12.dp))
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "$alertasActivas activas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$alertasCriticas Crítica",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusRed
                            )
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$alertasAdvertencia Advertencia",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusYellow
                            )
                        }
                    }
                }
            }

            // ── Encabezado Alertas recientes ────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Alertas.route)
                        }
                    )
                }
            }

            // ── Lista de alertas recientes ──────────────────────────────
            items(alertasRecientes) { alerta ->
                AlertItemCard(
                    alerta = alerta,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// ── Helper: ícono de acceso rápido ────────────────────────────────────────────
@Composable
private fun AccesoRapidoItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CityGridPrimary.copy(alpha = 0.1f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = CityGridPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
