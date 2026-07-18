package com.example.citygrid.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Calcula el tiempo transcurrido desde un timestamp dado.
 * Retorna los segundos transcurridos de forma reactiva.
 *
 * @param lastTimestamp Timestamp de la última actualización en milisegundos
 * @return Segundos transcurridos desde el último timestamp
 */
@Composable
fun rememberElapsedSeconds(lastTimestamp: Long): Int {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lastTimestamp) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(Constants.INTERVALO_CHECK_CONEXION_MS)
        }
    }

    return ((currentTime - lastTimestamp) / 1000).toInt().coerceAtLeast(0)
}

/**
 * Formatea un timestamp a string legible.
 *
 * @param timestamp Timestamp en milisegundos
 * @param pattern Patrón de formato (default: "dd/MM/yyyy · HH:mm 'hrs'")
 * @return String formateado
 */
fun formatTimestamp(timestamp: Long, pattern: String = "dd/MM/yyyy · HH:mm 'hrs'"): String {
    if (timestamp == 0L) return "Sin datos"
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Calcula tiempo relativo amigable (ej: "hace 5 minutos").
 *
 * @param timestamp Timestamp en milisegundos
 * @return String descriptivo del tiempo transcurrido
 */
fun tiempoRelativo(timestamp: Long): String {
    if (timestamp == 0L) return "Desconocido"

    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Hace menos de 1 minuto"
        minutes < 60 -> "Hace $minutes minuto${if (minutes > 1) "s" else ""}"
        hours < 24 -> "Hace $hours hora${if (hours > 1) "s" else ""}"
        else -> "Hace $days día${if (days > 1) "s" else ""}"
    }
}

/**
 * Obtiene texto descriptivo del estado de conexión basado en segundos inactivo.
 *
 * @param segundosInactivo Segundos desde la última actualización
 * @return Mensaje descriptivo del estado
 */
fun getEstadoConexionText(segundosInactivo: Int): String {
    return when {
        segundosInactivo < 30 -> "Conexión estable"
        segundosInactivo < 60 -> "Conexión intermitente"
        segundosInactivo < 300 -> "Señal débil (${segundosInactivo}s sin datos)"
        else -> "Desconectado (${segundosInactivo}s sin datos)"
    }
}
