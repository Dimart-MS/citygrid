package com.example.citygrid.ui.alertas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.citygrid.data.SessionManager
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.ui.theme.AmberAccent
import com.example.citygrid.ui.theme.AmberDark
import com.example.citygrid.ui.theme.AmberLight
import com.example.citygrid.ui.theme.BackgroundLight
import com.example.citygrid.ui.theme.CityGridPrimary
import com.example.citygrid.ui.theme.CityGridPrimaryDark
import com.example.citygrid.ui.theme.CityGridPrimaryLight
import com.example.citygrid.ui.theme.DividerColor
import com.example.citygrid.ui.theme.StatusBlue
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.SurfaceElevated
import com.example.citygrid.ui.theme.TextPrimary
import com.example.citygrid.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlertasScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: AlertasViewModel = viewModel { AlertasViewModel(sessionManager) }
    val listAlertas by viewModel.filteredAlertas.collectAsState()
    val activeFilter by viewModel.selectedFilter.collectAsState()
    val criticalCount by viewModel.criticalCount.collectAsState()
    val warningCount by viewModel.warningCount.collectAsState()

    var alertaParaAtender by remember { mutableStateOf<Alerta?>(null) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Dialogo para marcar como atendida
    if (alertaParaAtender != null) {
        AlertDialog(
            onDismissRequest = { alertaParaAtender = null },
            title = { Text("Atender Alerta", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Deseas marcar la alerta \"${alertaParaAtender?.titulo}\" como ATENDIDA?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        alertaParaAtender?.let { viewModel.marcarComoAtendida(it.id) }
                        alertaParaAtender = null
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CityGridPrimary)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { alertaParaAtender = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(22.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Título
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 }
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Alertas y Notificaciones",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Banner
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 80)) + slideInVertically(tween(400, delayMillis = 80)) { it / 3 }
            ) {
                BannerEstadoAlertas()
            }
        }

        // Resumen
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 160)) + slideInVertically(tween(400, delayMillis = 160)) { it / 3 }
            ) {
                TarjetaResumenAlertas(criticalCount = criticalCount, warningCount = warningCount)
            }
        }

        // Filtros
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300, delayMillis = 240))
            ) {
                FilaFiltrosAlertas(
                    selectedFilter = activeFilter,
                    onFilterSelected = { viewModel.setFilter(it) }
                )
            }
        }

        // Header lista
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300, delayMillis = 320))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alertas recientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Historial ›",
                        color = CityGridPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { }
                    )
                }
            }
        }

        if (listAlertas.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay alertas en este módulo",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            itemsIndexed(listAlertas) { index, alerta ->
                val timeString = remember(alerta.timestamp) {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    sdf.format(Date(alerta.timestamp))
                }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400, delayMillis = 400 + index * 60)) +
                            slideInVertically(tween(400, delayMillis = 400 + index * 60)) { it / 4 }
                ) {
                    TarjetaAlerta(
                        alerta = alerta,
                        hora = timeString,
                        onClick = { if (!alerta.atendida) alertaParaAtender = alerta }
                    )
                }
            }
        }
    }
}

@Composable
fun BannerEstadoAlertas() {
    val dateString = remember {
        val sdf = SimpleDateFormat("dd/MM/yyyy · HH:mm 'hrs'", Locale.getDefault())
        sdf.format(Date())
    }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateString,
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                com.example.citygrid.ui.components.StatusBadge(estado = "OPERANDO")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(StatusGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sistema general activo",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TarjetaResumenAlertas(criticalCount: Int, warningCount: Int) {
    val totalActivas = criticalCount + warningCount
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = totalActivas.toString(),
                color = if (totalActivas > 0) StatusRed else StatusGreen,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Alertas activas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (totalActivas > 0) "Requieren atención inmediata" else "No se requiere atención",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                EtiquetaNotificacion(
                    texto = "$criticalCount CRÍTICA",
                    backgroundColor = StatusRed.copy(alpha = 0.10f),
                    textColor = StatusRed
                )
                Spacer(modifier = Modifier.height(6.dp))
                EtiquetaNotificacion(
                    texto = "$warningCount ADVERTENCIA",
                    backgroundColor = AmberLight,
                    textColor = AmberDark
                )
            }
        }
    }
}

@Composable
fun FilaFiltrosAlertas(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val scrollFiltros = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollFiltros),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Todas", "Residuos", "Agua", "Alumbrado", "Sistema").forEach { filter ->
            ChipFiltro(
                texto = filter,
                activo = selectedFilter == filter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
fun ChipFiltro(texto: String, activo: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (activo) CityGridPrimary else Color.White
            )
            .border(
                width = 1.dp,
                color = if (activo) Color.Transparent else DividerColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = if (activo) Color.White else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun TarjetaAlerta(
    alerta: Alerta,
    hora: String,
    onClick: () -> Unit
) {
    val severityColor = when (alerta.tipo) {
        TipoAlerta.CRITICO     -> StatusRed
        TipoAlerta.ADVERTENCIA -> AmberAccent
        TipoAlerta.INFORMACION -> StatusBlue
        TipoAlerta.NORMAL      -> StatusGreen
    }

    val severidadStr = when (alerta.tipo) {
        TipoAlerta.CRITICO     -> "CRÍTICO"
        TipoAlerta.ADVERTENCIA -> "ADVERTENCIA"
        TipoAlerta.INFORMACION -> "INFORMACIÓN"
        TipoAlerta.NORMAL      -> "NORMAL"
    }

    val (statusBg, statusText) = if (alerta.atendida) {
        StatusGreen.copy(alpha = 0.12f) to StatusGreen
    } else {
        StatusRed.copy(alpha = 0.10f) to StatusRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Borde izquierdo semántico
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(
                        severityColor,
                        RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(severityColor.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = severidadStr,
                            color = severityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = hora,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = alerta.titulo,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alerta.descripcion,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "● ${alerta.sistema}",
                        color = CityGridPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    EtiquetaNotificacion(
                        texto = if (alerta.atendida) "ATENDIDA" else "PENDIENTE",
                        backgroundColor = statusBg,
                        textColor = statusText
                    )
                }
            }
        }
    }
}

@Composable
fun EtiquetaNotificacion(texto: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.4.sp
        )
    }
}