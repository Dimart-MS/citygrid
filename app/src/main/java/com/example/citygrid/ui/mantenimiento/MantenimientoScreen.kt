package com.example.citygrid.ui.mantenimiento

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.citygrid.data.SessionManager
import com.example.citygrid.model.db.DbMantenimiento
import com.example.citygrid.ui.components.EmptyState
import com.example.citygrid.ui.components.ShimmerPlaceholder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import com.example.citygrid.ui.theme.SurfaceCard
import com.example.citygrid.ui.theme.SurfaceElevated
import com.example.citygrid.ui.theme.DividerColor
import com.example.citygrid.ui.theme.TextSecondary
import com.example.citygrid.ui.theme.TextPrimary
import com.example.citygrid.ui.theme.CityGridPrimary
import com.example.citygrid.ui.theme.CityGridPrimaryDark
import com.example.citygrid.ui.theme.CityGridPrimaryLight
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusBlue
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.BackgroundLight

// NOTA: ColorPrincipal, ColorNocheLuna, etc. eliminadas del top-level para evitar conflictos.
// Se usan directamente los tokens de Color.kt.
private val MtoBgPreventivo  = StatusBlue.copy(alpha = 0.12f)
private val MtoTxtPreventivo = StatusBlue
private val MtoBgCorrectivo  = StatusRed.copy(alpha = 0.12f)
private val MtoTxtCorrectivo = StatusRed
private val MtoFondoInput    = Color(0xFFF2F6F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantenimientoScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: MantenimientoViewModel = viewModel { MantenimientoViewModel(sessionManager) }

    val mantenimientos by viewModel.mantenimientos.collectAsState()
    val usuariosMap by viewModel.usuariosMap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = BackgroundLight,
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarFormulario = true },
                containerColor = CityGridPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(bottom = 80.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Mantenimiento")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 }
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Control de Mantenimiento",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Banner
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 80)) + slideInVertically(tween(400, delayMillis = 80)) { it / 3 }
            ) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Registros Totales", color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${mantenimientos.size} mantenimientos",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300, delayMillis = 160))
            ) {
                Text(
                    "Historial de mantenimientos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (isLoading && mantenimientos.isEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DividerColor, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ShimmerPlaceholder(width = 44.dp, height = 44.dp, shape = CircleShape)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        ShimmerPlaceholder(width = 120.dp, height = 16.dp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        ShimmerPlaceholder(width = 180.dp, height = 12.dp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        ShimmerPlaceholder(width = 60.dp, height = 12.dp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        ShimmerPlaceholder(width = 80.dp, height = 20.dp, shape = RoundedCornerShape(10.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                ShimmerPlaceholder(height = 14.dp)
                                Spacer(modifier = Modifier.height(6.dp))
                                ShimmerPlaceholder(width = 220.dp, height = 14.dp)
                            }
                        }
                    }
                }
            } else if (mantenimientos.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Build,
                    titulo = "Sin mantenimientos",
                    subtitulo = "Toca el botón + para registrar el primer mantenimiento del sistema"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(mantenimientos) { index, mantenimiento ->
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(400, delayMillis = 240 + index * 60)) +
                                    slideInVertically(tween(400, delayMillis = 240 + index * 60)) { it / 4 }
                        ) {
                            val nombreResponsable = usuariosMap[mantenimiento.idUsuario] ?: "Usuario #${mantenimiento.idUsuario}"
                            TarjetaHistorialMantenimiento(mantenimiento, nombreResponsable)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        if (mostrarFormulario) {
            ModalBottomSheet(
                onDismissRequest = { mostrarFormulario = false },
                containerColor = Color.White,
                dragHandle = null,
                modifier = Modifier.fillMaxHeight(0.95f)
            ) {
                val nombreLogueado = usuariosMap[sessionManager.getIdUsuario()] ?: "Administrador"

                FormularioMantenimientoFigma(
                    nombreResponsable = nombreLogueado,
                    onGuardar = { idComponente, tipo, descripcion, fecha ->
                        viewModel.registrarMantenimiento(idComponente, tipo, descripcion, fecha)
                        mostrarFormulario = false
                    }
                )
            }
        }
    }
}

@Composable
fun TarjetaHistorialMantenimiento(mantenimiento: DbMantenimiento, responsable: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DividerColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono izquierdo
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(CityGridPrimaryLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = CityGridPrimary, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        obtenerNombreComponente(mantenimiento.idComponente),
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        "Módulo: ${obtenerModuloComponente(mantenimiento.idComponente)} · $responsable",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatearFechaCorta(mantenimiento.fechaMantenimiento),
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val tipoSeguro = mantenimiento.tipo ?: "PREVENTIVO"
                    val esPreventivo = tipoSeguro.uppercase() == "PREVENTIVO"

                    Surface(
                        color = if (esPreventivo) MtoBgPreventivo else MtoBgCorrectivo,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = tipoSeguro.uppercase(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = if (esPreventivo) MtoTxtPreventivo else MtoTxtCorrectivo,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            if (mantenimiento.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mantenimiento.descripcion,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioMantenimientoFigma(
    nombreResponsable: String,
    onGuardar: (Int, String, String, String) -> Unit
) {
    val listaComponentes = listOf(
        Pair(1, "Sensor Ultrasónico (Residuos)"),
        Pair(2, "Bomba de Agua (Agua)"),
        Pair(3, "ESP32 Principal (Alumbrado)"),
        Pair(4, "Relay de Control (Alumbrado)")
    )

    var componenteSeleccionado by remember { mutableStateOf(listaComponentes[0]) }
    var expandirComponentes by remember { mutableStateOf(false) }

    var tipoSeleccionado by remember { mutableStateOf("Preventivo") }
    var expandirDropdown by remember { mutableStateOf(false) }

    var descripcion by remember { mutableStateOf("") }

    var fechaEditada by remember { mutableStateOf(java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(Date())) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(colors = listOf(CityGridPrimaryDark, Color(0xFF0A3D62)))
                )
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Nuevo registro de mantenimiento", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Column(modifier = Modifier.padding(24.dp)) {


            Text("Dispositivo", fontSize = 12.sp, color = CityGridPrimaryDark)
            Spacer(modifier = Modifier.height(4.dp))
            Box {
                InputFigma(
                    value = componenteSeleccionado.second,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = CityGridPrimary) }
                )
                Box(modifier = Modifier.matchParentSize().clickable { expandirComponentes = true })

                DropdownMenu(
                    expanded = expandirComponentes,
                    onDismissRequest = { expandirComponentes = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    listaComponentes.forEach { comp ->
                        DropdownMenuItem(
                            text = { Text(comp.second, color = Color.Black) },
                            onClick = {
                                componenteSeleccionado = comp
                                expandirComponentes = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                // DROPDOWN: TIPO
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tipo", fontSize = 12.sp, color = CityGridPrimaryDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        InputFigma(
                            value = tipoSeleccionado,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = CityGridPrimary) }
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { expandirDropdown = true })

                        DropdownMenu(
                            expanded = expandirDropdown,
                            onDismissRequest = { expandirDropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(text = { Text("Preventivo", color = Color.Black) }, onClick = { tipoSeleccionado = "Preventivo"; expandirDropdown = false })
                            DropdownMenuItem(text = { Text("Correctivo", color = Color.Black) }, onClick = { tipoSeleccionado = "Correctivo"; expandirDropdown = false })
                        }
                    }
                }


                Column(modifier = Modifier.weight(1f)) {
                    Text("Fecha", fontSize = 12.sp, color = CityGridPrimaryDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        InputFigma(
                            value = fechaEditada,
                            onValueChange = {},
                            readOnly = true
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { mostrarDatePicker = true })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Responsable", fontSize = 12.sp, color = CityGridPrimaryDark)
            Spacer(modifier = Modifier.height(4.dp))
            InputFigma(value = nombreResponsable, onValueChange = {}, readOnly = true)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Descripción", fontSize = 12.sp, color = CityGridPrimaryDark)
            Spacer(modifier = Modifier.height(4.dp))
            TextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = { Text("Se reemplazó cable de...", color = Color.Gray, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MtoFondoInput,
                    unfocusedContainerColor = MtoFondoInput,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (descripcion.isNotEmpty()) {
                        onGuardar(componenteSeleccionado.first, tipoSeleccionado, descripcion, fechaEditada)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CityGridPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Guardar Registro", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        fechaEditada = formatter.format(date)
                    }
                    mostrarDatePicker = false
                }) {
                    Text("Aceptar", color = CityGridPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    todayDateBorderColor = CityGridPrimary,
                    selectedDayContainerColor = CityGridPrimary
                )
            )
        }
    }
}

@Composable
fun InputFigma(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        placeholder = { Text(placeholder, color = Color.Gray, fontSize = 13.sp) },
        trailingIcon = trailingIcon,
        singleLine = true,
        modifier = modifier.fillMaxWidth().height(52.dp),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color.Black),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MtoFondoInput,
            unfocusedContainerColor = MtoFondoInput,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

private fun formatearFechaCorta(fechaStr: String?): String = try {
    OffsetDateTime.parse(fechaStr).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
} catch (e: Exception) {
    "--/--/----"
}

fun obtenerNombreComponente(id: Int): String {
    return when(id) {
        1 -> "Sensor Ultrasónico"
        2 -> "Bomba de Agua"
        3 -> "ESP32 Principal"
        4 -> "Relay de control"
        else -> "Componente #$id"
    }
}

fun obtenerModuloComponente(id: Int): String {
    return when(id) {
        1 -> "Residuos"
        2 -> "Agua"
        3, 4 -> "Alumbrado"
        else -> "General"
    }
}
