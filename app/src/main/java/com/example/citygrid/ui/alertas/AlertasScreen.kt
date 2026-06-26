package com.example.citygrid.ui.alertas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.TipoAlerta
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlertasScreen(
    viewModel: AlertasViewModel = viewModel()
) {
    val listAlertas by viewModel.filteredAlertas.collectAsState()
    val activeFilter by viewModel.selectedFilter.collectAsState()
    val criticalCount by viewModel.criticalCount.collectAsState()
    val warningCount by viewModel.warningCount.collectAsState()

    var alertaParaAtender by remember { mutableStateOf<Alerta?>(null) }

    // Dialogo para marcar una alerta como atendida
    if (alertaParaAtender != null) {
        AlertDialog(
            onDismissRequest = { alertaParaAtender = null },
            title = { Text("Atender Alerta") },
            text = { Text("¿Deseas marcar la alerta \"${alertaParaAtender?.titulo}\" como ATENDIDA?") },
            confirmButton = {
                Button(
                    onClick = {
                        alertaParaAtender?.let { viewModel.marcarComoAtendida(it.id) }
                        alertaParaAtender = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A8CC))
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { alertaParaAtender = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F9)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SeccionBienvenidaAlertas()
        }

        item {
            BannerEstadoAlertas()
        }

        item {
            TarjetaResumenAlertas(criticalCount = criticalCount, warningCount = warningCount)
        }

        item {
            FilaFiltrosAlertas(selectedFilter = activeFilter, onFilterSelected = { viewModel.setFilter(it) })
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alertas recientes",
                    color = Color(0xFF2D3748),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Historial ›",
                    color = Color(0xFF00A8CC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { }
                )
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
                    Text("No hay alertas en este módulo", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            items(listAlertas) { alerta ->
                val timeString = remember(alerta.timestamp) {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    sdf.format(Date(alerta.timestamp))
                }

                TarjetaAlerta(
                    alerta = alerta,
                    hora = timeString,
                    onClick = {
                        if (!alerta.atendida) {
                            alertaParaAtender = alerta
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SeccionBienvenidaAlertas() {
    Column {
        Text(
            text = "Bienvenido de vuelta",
            color = Color(0xFF718096),
            fontSize = 13.sp
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFF00A8CC), fontWeight = FontWeight.Bold)) {
                    append("Alertas y Notificaciones")
                }
                withStyle(style = SpanStyle(color = Color(0xFF2D3748), fontWeight = FontWeight.Bold)) {
                    append(" · CityGrid")
                }
            },
            fontSize = 20.sp
        )
    }
}

@Composable
fun BannerEstadoAlertas() {
    val dateString = remember {
        val sdf = SimpleDateFormat("dd/06/yyyy · HH:mm 'hrs'", Locale.getDefault())
        sdf.format(Date())
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F3540))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateString,
                    color = Color(0xFFCBD5E0),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E3A47), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF00A896), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "✓ Operando",
                        color = Color(0xFF00A896),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF00E5FF), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sistema general activo",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TarjetaResumenAlertas(criticalCount: Int, warningCount: Int) {
    val totalActivas = criticalCount + warningCount
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = totalActivas.toString(),
                color = if (totalActivas > 0) Color(0xFFE53E3E) else Color(0xFF2F855A),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Alertas activas",
                    color = Color(0xFF2D3748),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (totalActivas > 0) "Requieren atención inmediata" else "No se requiere atención",
                    color = Color(0xFF718096),
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                EtiquetaNotificacion(texto = "$criticalCount CRÍTICA", backgroundColor = Color(0xFFFFE5E5), textColor = Color(0xFFC53030))
                Spacer(modifier = Modifier.height(6.dp))
                EtiquetaNotificacion(texto = "$warningCount ADVERTENCIA", backgroundColor = Color(0xFFFEFCBF), textColor = Color(0xFFB7791F))
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
            .background(if (activo) Color(0xFF00A8CC) else Color.White)
            .border(
                width = 1.dp,
                color = if (activo) Color.Transparent else Color(0xFFCBD5E0),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = if (activo) Color.White else Color(0xFF00A8CC),
            fontSize = 13.sp,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TarjetaAlerta(
    alerta: Alerta,
    hora: String,
    onClick: () -> Unit
) {
    val severidadStr = alerta.tipo.name // CRITICO, ADVERTENCIA, INFORMACION, NORMAL
    val (iconBg, badgeBg, badgeText) = when (alerta.tipo) {
        TipoAlerta.CRITICO -> Triple(Color(0xFFFFE5E5), Color(0xFFFFE5E5), Color(0xFFC53030))
        TipoAlerta.ADVERTENCIA -> Triple(Color(0xFFFEFCBF), Color(0xFFFEFCBF), Color(0xFFB7791F))
        TipoAlerta.INFORMACION -> Triple(Color(0xFFEBF8FF), Color(0xFFEBF8FF), Color(0xFF2B6CB0))
        else -> Triple(Color(0xFFC6F6D5), Color(0xFFC6F6D5), Color(0xFF2F855A))
    }

    val estadoStr = if (alerta.atendida) "ATENDIDA" else "PENDIENTE"
    val (statusBg, statusText) = if (alerta.atendida) {
        Pair(Color(0xFFC6F6D5), Color(0xFF2F855A))
    } else {
        Pair(Color(0xFFFFE5E5), Color(0xFFC53030))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EtiquetaNotificacion(texto = severidadStr, backgroundColor = badgeBg, textColor = badgeText)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = hora, color = Color(0xFFA0AEC0), fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = alerta.titulo,
                    color = Color(0xFF2D3748),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alerta.descripcion,
                    color = Color(0xFF718096),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = alerta.sistema,
                        color = Color(0xFF00A8CC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    EtiquetaNotificacion(texto = estadoStr, backgroundColor = statusBg, textColor = statusText)
                }
            }
        }
    }
}

@Composable
fun EtiquetaNotificacion(texto: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}