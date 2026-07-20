package com.example.citygrid.ui.configuracion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.citygrid.ui.theme.*

@Composable
fun ConfiguracionScreen(
    viewModel: ConfiguracionViewModel,
    onLogout: () -> Unit,
    onThemeChanged: () -> Unit = {}
) {
    var darkMode by remember { mutableStateOf(viewModel.isDarkMode()) }
    val nombre = viewModel.getNombre()
    val correo = viewModel.getCorreo()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Título
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 }
        ) {
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Perfil
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400, delayMillis = 80)) + slideInVertically(tween(400, delayMillis = 80)) { it / 3 }
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(CityGridPrimary, CityGridPrimaryMid)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nombre.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (correo.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = correo,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Apariencia
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400, delayMillis = 160)) + slideInVertically(tween(400, delayMillis = 160)) { it / 3 }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderWidth.thin, DividerColor, RoundedCornerShape(Radius.lg)),
                shape = RoundedCornerShape(Radius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.sm)
            ) {
                Column(modifier = Modifier.padding(Spacing.xl)) {
                    Text(
                        text = "APARIENCIA",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FilaConfiguracion(
                        icono = if (darkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        titulo = "Modo oscuro",
                        descripcion = if (darkMode) "Tema oscuro activado" else "Tema claro activado",
                        accion = {
                            Switch(
                                checked = darkMode,
                                onCheckedChange = { activo ->
                                    darkMode = activo
                                    viewModel.setDarkMode(activo)
                                    onThemeChanged()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = CityGridPrimary,
                                    checkedThumbColor = Color.White
                                )
                            )
                        }
                    )
                }
            }
        }

        // Información de la app
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
                Column(modifier = Modifier.padding(Spacing.xl)) {
                    Text(
                        text = "ACERCA DE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FilaConfiguracion(
                        icono = Icons.Default.Info,
                        titulo = "Versión",
                        descripcion = "1.0.0",
                        accion = {}
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = DividerColor
                    )
                    FilaConfiguracion(
                        icono = Icons.Default.Build,
                        titulo = "Framework",
                        descripcion = "Jetpack Compose + Material 3",
                        accion = {}
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = DividerColor
                    )
                    FilaConfiguracion(
                        icono = Icons.Default.Cloud,
                        titulo = "Backend",
                        descripcion = "Supabase + MQTT (HiveMQ)",
                        accion = {}
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = DividerColor
                    )
                    FilaConfiguracion(
                        icono = Icons.Default.Devices,
                        titulo = "Dispositivos",
                        descripcion = "ESP32 con sensores IoT",
                        accion = {}
                    )
                }
            }
        }

        // Cerrar sesión
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400, delayMillis = 320)) + slideInVertically(tween(400, delayMillis = 320)) { it / 3 }
        ) {
            var showConfirm by remember { mutableStateOf(false) }

            if (showConfirm) {
                AlertDialog(
                    onDismissRequest = { showConfirm = false },
                    title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold) },
                    text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showConfirm = false
                                viewModel.cerrarSesion()
                                onLogout()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                        ) {
                            Text("Cerrar sesión")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirm = false }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                    },
                    shape = RoundedCornerShape(22.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(StatusRed.copy(alpha = 0.06f))
                    .border(BorderWidth.thin, StatusRed.copy(alpha = 0.15f), RoundedCornerShape(Radius.lg))
                    .clickable { showConfirm = true }
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = StatusRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cerrar sesión",
                        color = StatusRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
private fun FilaConfiguracion(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    accion: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = CityGridPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        accion()
    }
}
