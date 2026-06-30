package com.example.citygrid.ui.mantenimiento

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import com.example.citygrid.model.db.DbMantenimiento
import com.example.citygrid.ui.components.CityGridTopBar
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

val ColorPrincipal = Color(0xFF0FA3B1)
val ColorNocheLuna = Color(0xFF284553)
val ColorBotonGuardar = Color(0xFF2A9D8F)
val ColorFondoInput = Color(0xFFF2F6F9)
val ColorIconoFondo = Color(0xFFE8F1F2)

val BgPreventivo = Color(0xFFE1F5FE)
val TxtPreventivo = Color(0xFF0277BD)
val BgCorrectivo = Color(0xFFFFEBEE)
val TxtCorrectivo = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantenimientoScreen(viewModel: MantenimientoViewModel = viewModel()) {
    val mantenimientos by viewModel.mantenimientos.collectAsState()
    val usuariosMap by viewModel.usuariosMap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarFormulario = true },
                containerColor = ColorPrincipal,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Mantenimiento")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("Bienvenido", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = ColorPrincipal, fontWeight = FontWeight.Bold)) { append("Control de Mantenimiento") }
                    withStyle(style = SpanStyle(color = ColorNocheLuna, fontWeight = FontWeight.Bold)) { append(" - CityGrid") }
                }, style = MaterialTheme.typography.headlineSmall)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorNocheLuna),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Registros Totales", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${mantenimientos.size} mantenimientos", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.Engineering, contentDescription = null, tint = ColorPrincipal, modifier = Modifier.size(48.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Historial de mantenimientos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ColorNocheLuna)
            }

            if (isLoading && mantenimientos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColorPrincipal)
                }
            } else if (mantenimientos.isEmpty()) {
                Text("No hay mantenimientos registrados.", color = Color.Gray)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mantenimientos) { mantenimiento ->
                        val nombreResponsable = usuariosMap[mantenimiento.idUsuario] ?: "Usuario #${mantenimiento.idUsuario}"
                        TarjetaHistorialMantenimiento(mantenimiento, nombreResponsable)
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
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
                val nombreLogueado = usuariosMap[viewModel.idUsuarioLogueado] ?: "Administrador"

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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono izquierdo
                Box(
                    modifier = Modifier.size(48.dp).background(ColorIconoFondo, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = ColorPrincipal)
                }

                Spacer(modifier = Modifier.width(12.dp))


                Column(modifier = Modifier.weight(1f)) {
                    Text(obtenerNombreComponente(mantenimiento.idComponente), fontWeight = FontWeight.Bold, color = ColorNocheLuna, fontSize = 14.sp)
                    Text("Módulo: ${obtenerModuloComponente(mantenimiento.idComponente)} · Resp: $responsable", color = Color.Gray, fontSize = 11.sp, lineHeight = 14.sp)
                }


                Column(horizontalAlignment = Alignment.End) {
                    Text(formatearFechaCorta(mantenimiento.fechaMantenimiento), color = Color.Gray, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    val tipoSeguro = mantenimiento.tipo ?: "PREVENTIVO"
                    val esPreventivo = tipoSeguro.uppercase() == "PREVENTIVO"

                    Surface(
                        color = if (esPreventivo) BgPreventivo else BgCorrectivo,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = tipoSeguro.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (esPreventivo) TxtPreventivo else TxtCorrectivo,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (mantenimiento.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Desc: ${mantenimiento.descripcion}",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(start = 60.dp)
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
            modifier = Modifier.fillMaxWidth().background(ColorNocheLuna).padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Nuevo registro de mantenimiento", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(24.dp)) {


            Text("Dispositivo", fontSize = 12.sp, color = ColorNocheLuna)
            Spacer(modifier = Modifier.height(4.dp))
            Box {
                InputFigma(
                    value = componenteSeleccionado.second,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = ColorPrincipal) }
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
                    Text("Tipo", fontSize = 12.sp, color = ColorNocheLuna)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        InputFigma(
                            value = tipoSeleccionado,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = ColorPrincipal) }
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
                    Text("Fecha", fontSize = 12.sp, color = ColorNocheLuna)
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

            Text("Responsable", fontSize = 12.sp, color = ColorNocheLuna)
            Spacer(modifier = Modifier.height(4.dp))
            InputFigma(value = nombreResponsable, onValueChange = {}, readOnly = true)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Descripción", fontSize = 12.sp, color = ColorNocheLuna)
            Spacer(modifier = Modifier.height(4.dp))
            TextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = { Text("Se reemplazó cable de...", color = Color.Gray, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ColorFondoInput,
                    unfocusedContainerColor = ColorFondoInput,
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
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBotonGuardar),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Registro", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    Text("Aceptar", color = ColorPrincipal, fontWeight = FontWeight.Bold)
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
                    todayDateBorderColor = ColorPrincipal,
                    selectedDayContainerColor = ColorPrincipal
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
            focusedContainerColor = ColorFondoInput,
            unfocusedContainerColor = ColorFondoInput,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp)
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