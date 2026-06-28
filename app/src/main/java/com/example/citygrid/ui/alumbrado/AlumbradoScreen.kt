package com.example.citygrid.ui.alumbrado

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.citygrid.model.db.DbLecturaLuminaria
import com.example.citygrid.ui.components.CityGridTopBar
import com.example.citygrid.ui.components.StatusBadge
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


val ColorPrincipal = Color(0xFF0FA3B1)
val ColorEstadoFondo = Color(0x402A9D8F)
val ColorPuntoActivo = Color(0xFF2EE8A5)
val ColorNocheLuna = Color(0xFF284553)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumbradoScreen(viewModel: AlumbradoViewModel = viewModel()) {
    val state by viewModel.alumbradoState.collectAsState()
    val historial by viewModel.historial.collectAsState()
    var mostrarHistorial by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        contentWindowInsets = WindowInsets(0.dp)
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
                    withStyle(style = SpanStyle(color = ColorPrincipal, fontWeight = FontWeight.Bold)) { append("Alumbrado Inteligente") }
                    withStyle(style = SpanStyle(color = ColorNocheLuna, fontWeight = FontWeight.Bold)) { append(" - CityGrid") }
                }, style = MaterialTheme.typography.headlineSmall)
            }


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorNocheLuna),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(formatTimestamp(state.ultimaActualizacion), style = MaterialTheme.typography.bodySmall, color = Color.LightGray)

                        // --- NUEVO DISEÑO "OPERANDO" DE FIGMA ---
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
                        Box(modifier = Modifier.size(8.dp).background(ColorPuntoActivo, androidx.compose.foundation.shape.CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (state.estadoOn) "Sistema de alumbrado activo" else "Sistema inactivo", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }


            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = if (state.estadoOn) ColorPrincipal else ColorNocheLuna, modifier = Modifier.size(32.dp))
                        Text("Estado", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Text(if (state.estadoOn) "ON" else "OFF", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (state.estadoOn) ColorPrincipal else ColorNocheLuna)
                        Text("Encendido automático", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(if (state.condicionNoche) Icons.Default.ModeNight else Icons.Default.WbSunny, contentDescription = null, tint = if (state.condicionNoche) ColorNocheLuna else ColorPrincipal, modifier = Modifier.size(32.dp))
                        Text("Condicion", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Text(if (state.condicionNoche) "Noche" else "Día", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (state.condicionNoche) ColorNocheLuna else ColorPrincipal)
                        Text("Detectada por LDR", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }


            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow(Icons.Default.Sensors, "Sensor LDR", "${state.ldrLux} Lux")
                    DetailRow(Icons.Default.Lightbulb, "Luminarias activas", "${state.luminariasActivas} luminarias")
                    DetailRow(Icons.Default.Settings, "Modo", state.modo)
                }
            }


            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Eventos recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { mostrarHistorial = true }) { Text("Historial >", color = ColorPrincipal) }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                historial.take(3).forEach { lectura -> TarjetaEventoMini(lectura) }
            }

            Text("Última actualización: ${formatTimestamp(System.currentTimeMillis())}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
        }


        if (mostrarHistorial) {
            ModalBottomSheet(
                onDismissRequest = { mostrarHistorial = false },
                containerColor = Color.White,
                modifier = Modifier.fillMaxHeight(0.85f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Historial Completo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ColorNocheLuna)
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
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ColorPrincipal, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, modifier = Modifier.weight(1f), color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TarjetaEventoMini(lectura: DbLecturaLuminaria) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (lectura.valorLdr < 100) Icons.Default.ModeNight else Icons.Default.WbSunny, null, tint = ColorPrincipal)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(if (lectura.valorLdr < 100) "Lectura Nocturna" else "Lectura Diurna", fontWeight = FontWeight.Bold)
                Text("Valor: ${lectura.valorLdr} Lux | ${formatearFechaBd(lectura.fechaHora)}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun TarjetaHistorialLDR(lectura: DbLecturaLuminaria) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Lectura del Sensor", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(formatearFechaBdCompleta(lectura.fechaHora), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${lectura.valorLdr} Lux", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ColorPrincipal)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (lectura.valorLdr < 100) Icons.Default.ModeNight else Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = ColorPrincipal,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String = java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
private fun formatearFechaBd(fechaStr: String?): String = try { OffsetDateTime.parse(fechaStr).format(DateTimeFormatter.ofPattern("HH:mm")) } catch (e: Exception) { "--:--" }
private fun formatearFechaBdCompleta(fechaStr: String?): String = try { OffsetDateTime.parse(fechaStr).format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")) } catch (e: Exception) { fechaStr ?: "" }