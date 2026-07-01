package com.example.citygrid.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.citygrid.ui.theme.DividerColor
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.StatusYellow
import com.example.citygrid.ui.theme.TextSecondary
import kotlin.math.max
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Gráfico de semicírculo (arco de 180°) — CityGrid v2.
 *
 * El color del arco depende del valor:
 *  - >= 85  → StatusRed    (crítico)
 *  - >= 50  → StatusYellow (medio / advertencia)
 *  - otro   → StatusGreen  (normal / óptimo)
 *
 * Mejoras v2: stroke más grueso (28dp), animación de entrada, arco
 * con glow sutil (shadow track más ancho y traslucido).
 */
@Composable
fun SemiCircleChart(
    porcentaje: Int,
    modifier: Modifier = Modifier,
    label: String = "",
    color: Color? = null
) {
    val safeProgress = porcentaje.coerceIn(0, 100)

    val displayColor = color ?: when {
        safeProgress >= 85 -> StatusRed
        safeProgress >= 50 -> StatusYellow
        else               -> StatusGreen
    }

    val animatedSweep by animateFloatAsState(
        targetValue = (safeProgress / 100f) * 180f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "semiCircleSweep"
    )

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.size(190.dp)
    ) {
        Canvas(modifier = Modifier.size(190.dp)) {
            val strokeWidth    = 28.dp.toPx()
            val glowWidth      = 36.dp.toPx()
            val radius         = max(1f, (size.minDimension / 2f) - strokeWidth / 2f)
            val topLeft        = Offset(
                x = (size.width / 2f) - radius,
                y = (size.height / 2f) - radius
            )
            val arcSize = Size(radius * 2f, radius * 2f)

            // Track (fondo)
            drawArc(
                color = DividerColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            // Glow del arco activo
            if (animatedSweep > 0f) {
                drawArc(
                    color = displayColor.copy(alpha = 0.15f),
                    startAngle = 180f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = glowWidth, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }

            // Progreso principal
            if (animatedSweep > 0f) {
                drawArc(
                    color = displayColor,
                    startAngle = 180f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.size(190.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$safeProgress%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = displayColor
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
