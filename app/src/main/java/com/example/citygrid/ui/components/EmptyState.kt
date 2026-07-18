package com.example.citygrid.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.citygrid.ui.theme.CityGridPrimary
import com.example.citygrid.ui.theme.CityGridPrimaryLight
import com.example.citygrid.ui.theme.TextPrimary
import com.example.citygrid.ui.theme.TextSecondary

/**
 * Estado vacío animado y reutilizable para todas las pantallas de la app.
 *
 * @param icon          Ícono a mostrar (de Material Icons).
 * @param titulo        Título principal del estado vacío.
 * @param subtitulo     Texto secundario opcional debajo del título.
 * @param accion        Slot opcional para un botón de acción (ej. "Reintentar").
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    titulo: String,
    subtitulo: String? = null,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    val transition = rememberInfiniteTransition(label = "emptyStatePulse")
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val haloAlpha by transition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Halo exterior animado
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulse)
                        .alpha(haloAlpha)
                        .background(CityGridPrimary, CircleShape)
                )
                // Halo interior
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(CityGridPrimaryLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CityGridPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            if (subtitulo != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            if (accion != null) {
                Spacer(Modifier.height(20.dp))
                accion()
            }
        }
    }
}

/**
 * Estado de carga con shimmer circular — para cuando se está esperando respuesta de red.
 */
@Composable
fun LoadingState(
    mensaje: String = "Cargando...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = CityGridPrimary,
                strokeWidth = 3.dp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

/** Utilidad: tiempo relativo en español ("hace 5 min", "ayer"). */
fun tiempoRelativo(timestamp: Long, ahora: Long = System.currentTimeMillis()): String {
    if (timestamp <= 0L) return "—"
    val diff = ahora - timestamp
    val segundos = diff / 1000
    val minutos = segundos / 60
    val horas = minutos / 60
    val dias = horas / 24
    return when {
        segundos < 5 -> "Justo ahora"
        segundos < 60 -> "hace $segundos s"
        minutos < 60 -> "hace $minutos min"
        horas < 24 -> "hace $horas h"
        dias == 1L -> "ayer"
        dias < 7 -> "hace $dias días"
        else -> {
            val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}
