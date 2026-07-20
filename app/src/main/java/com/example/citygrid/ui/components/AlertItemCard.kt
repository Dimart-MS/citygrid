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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.citygrid.ui.theme.StatusBlue
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.TextPrimary
import com.example.citygrid.ui.theme.TextSecondary
import com.example.citygrid.ui.theme.TextTertiary
import com.example.citygrid.ui.theme.BorderWidth
import com.example.citygrid.ui.theme.Radius
import com.example.citygrid.ui.theme.Spacing
import com.example.citygrid.ui.theme.IconSize
import com.example.citygrid.ui.theme.AvatarSize
import com.example.citygrid.ui.theme.Elevation
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
        modifier = modifier
            .fillMaxWidth()
            .interactiveSurface(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(Radius.md),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de sistema en círculo
            Box(
                modifier = Modifier
                    .size(AvatarSize.lg)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(IconSize.lg)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alerta.titulo.ifBlank { "Alerta" },
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge de severidad
                    Surface(
                        shape = RoundedCornerShape(Radius.sm),
                        color = severityColor.copy(alpha = 0.10f)
                    ) {
                        Text(
                            text = severityLabel,
                            color = severityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
                        )
                    }
                    if (hora.isNotBlank()) {
                        Spacer(modifier = Modifier.width(Spacing.sm))
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
                tint = TextTertiary,
                modifier = Modifier.size(IconSize.lg)
            )
        }
    }
}
