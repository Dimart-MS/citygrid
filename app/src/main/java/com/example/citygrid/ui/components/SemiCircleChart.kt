package com.example.citygrid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.StatusYellow
import com.example.citygrid.ui.theme.TextSecondary
import kotlin.math.max

/**
 * Gráfico de semicírculo (arco de 180°) que muestra un porcentaje.
 *
 * El color del arco depende del valor:
 *  - >= 85  -> StatusRed    (crítico)
 *  - >= 50  -> StatusYellow (medio / advertencia)
 *  - otro   -> StatusGreen  (normal)
 *
 * Implementado con Canvas (sin dependencias externas).
 */
@Composable
fun SemiCircleChart(
    porcentaje: Int,
    modifier: Modifier = Modifier,
    label: String = ""
) {
    val safeProgress = porcentaje.coerceIn(0, 100)

    val color = when {
        safeProgress >= 85 -> StatusRed
        safeProgress >= 50 -> StatusYellow
        else                -> StatusGreen
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.size(180.dp)
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 24.dp.toPx()
            // Garantiza radio positivo incluso si el Canvas fuera muy chico.
            val radius = max(1f, (size.minDimension / 2f) - strokeWidth / 2f)
            val topLeft = Offset(
                x = (size.width / 2f) - radius,
                y = (size.height / 2f) - radius
            )
            val arcSize = Size(radius * 2f, radius * 2f)

            // Fondo (arco completo 180°)
            drawArc(
                color = Color(0xFFE0E0E0),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            // Progreso
            val sweep = (safeProgress / 100f) * 180f
            if (sweep > 0f) {
                drawArc(
                    color = color,
                    startAngle = 180f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.size(180.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "$safeProgress%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
