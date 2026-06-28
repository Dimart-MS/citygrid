package com.example.citygrid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.TipoAlerta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertItemCard(
    alerta: Alerta,
    modifier: Modifier = Modifier
) {
    val severityColor = when (alerta.tipo) {
        TipoAlerta.CRITICO     -> Color(0xFFEF5350)
        TipoAlerta.ADVERTENCIA -> Color(0xFFFFA726)
        TipoAlerta.INFORMACION -> Color(0xFF42A5F5)
        TipoAlerta.NORMAL      -> Color(0xFF66BB6A)
    }

    val severityLabel = when (alerta.tipo) {
        TipoAlerta.CRITICO     -> "CRÍTICO"
        TipoAlerta.ADVERTENCIA -> "ADVERTENCIA"
        TipoAlerta.INFORMACION -> "INFORMACIÓN"
        TipoAlerta.NORMAL      -> "NORMAL"
    }

    val (icono, iconBg, iconTint) = when (alerta.sistema) {
        "Residuos" -> Triple(Icons.Filled.Delete, Color(0xFFFFEBEE), Color(0xFFEF5350))
        "Agua"     -> Triple(Icons.Filled.WaterDrop, Color(0xFFFFF8E1), Color(0xFFFFA726))
        else       -> Triple(Icons.Filled.Info, Color(0xFFE3F2FD), Color(0xFF42A5F5))
    }

    val hora = if (alerta.timestamp > 0L) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(alerta.timestamp))
    } else ""

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(severityColor)
            )
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = alerta.titulo.ifBlank { "Alerta" },
                    color = Color(0xFF212121),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = severityLabel,
                        color = severityColor,
                        fontSize = 11.sp
                    )
                    if (hora.isNotBlank()) {
                        Text(
                            text = " · ",
                            color = Color(0xFF9E9E9E),
                            fontSize = 11.sp
                        )
                        Text(
                            text = hora,
                            color = Color(0xFF9E9E9E),
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
        }
    }
}
