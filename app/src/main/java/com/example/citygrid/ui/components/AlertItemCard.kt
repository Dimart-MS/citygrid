package com.example.citygrid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
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
import com.example.citygrid.ui.theme.AmberAccent
import com.example.citygrid.ui.theme.CityGridPrimary
import com.example.citygrid.ui.theme.SurfaceElevated
import com.example.citygrid.ui.theme.StatusBlue
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.StatusYellow
import com.example.citygrid.ui.theme.TextPrimary
import com.example.citygrid.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertItemCard(
    alerta: Alerta,
    modifier: Modifier = Modifier
) {
    val severityColor = when (alerta.tipo) {
        TipoAlerta.CRITICO     -> StatusRed
        TipoAlerta.ADVERTENCIA -> AmberAccent
        TipoAlerta.INFORMACION -> StatusBlue
        TipoAlerta.NORMAL      -> StatusGreen
    }

    val severityLabel = when (alerta.tipo) {
        TipoAlerta.CRITICO     -> "CRÍTICO"
        TipoAlerta.ADVERTENCIA -> "ADVERTENCIA"
        TipoAlerta.INFORMACION -> "INFORMACIÓN"
        TipoAlerta.NORMAL      -> "NORMAL"
    }

    val (icono, iconBg, iconTint) = when (alerta.sistema) {
        "Residuos" -> Triple(Icons.Filled.Delete,    StatusRed.copy(alpha = 0.10f),    StatusRed)
        "Agua"     -> Triple(Icons.Filled.WaterDrop, CityGridPrimary.copy(alpha = 0.10f), CityGridPrimary)
        "Alumbrado"-> Triple(Icons.Filled.WbSunny,   AmberAccent.copy(alpha = 0.10f),  AmberAccent)
        else       -> Triple(Icons.Filled.Info,      StatusBlue.copy(alpha = 0.10f),   StatusBlue)
    }

    val hora = if (alerta.timestamp > 0L) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(alerta.timestamp))
    } else ""

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Borde izquierdo de severidad (5dp)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(
                        severityColor,
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
            Spacer(Modifier.width(14.dp))
            // Ícono de sistema
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = alerta.titulo.ifBlank { "Alerta" },
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                severityColor.copy(alpha = 0.12f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = severityLabel,
                            color = severityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp
                        )
                    }
                    if (hora.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = hora,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 2.dp)
            )
            Spacer(Modifier.width(12.dp))
        }
    }
}
