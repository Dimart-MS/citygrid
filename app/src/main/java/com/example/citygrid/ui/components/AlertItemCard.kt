package com.example.citygrid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.citygrid.ui.theme.CityGridGreen
import com.example.citygrid.ui.theme.StatusBlue
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.StatusYellow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.TipoAlerta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tarjeta reutilizable para mostrar una [Alerta] en listas (Dashboard, Alertas, etc.).
 */
@Composable
fun AlertItemCard(
    alerta: Alerta,
    modifier: Modifier = Modifier
) {
    val iconTint = when (alerta.tipo) {
        TipoAlerta.CRITICO     -> StatusRed
        TipoAlerta.ADVERTENCIA -> StatusYellow
        TipoAlerta.INFORMACION -> StatusBlue
        TipoAlerta.NORMAL      -> StatusGreen
    }

    val tipoLabel = when (alerta.tipo) {
        TipoAlerta.CRITICO     -> "CRÍTICO"
        TipoAlerta.ADVERTENCIA -> "ADVERTENCIA"
        TipoAlerta.INFORMACION -> "INFORMACIÓN"
        TipoAlerta.NORMAL      -> "NORMAL"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = iconTint
                    ) {
                        Text(
                            text = tipoLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (alerta.timestamp > 0L) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = formatHora(alerta.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = alerta.titulo.ifBlank { "Alerta" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (alerta.descripcion.isNotBlank()) {
                    Text(
                        text = alerta.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (alerta.sistema.isNotBlank()) {
                    Text(
                        text = alerta.sistema,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = CityGridGreen
                    )
                }
            }
        }
    }
}

private fun formatHora(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(timestamp))
