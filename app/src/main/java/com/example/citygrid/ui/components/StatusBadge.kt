package com.example.citygrid.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.citygrid.ui.theme.AmberAccent
import com.example.citygrid.ui.theme.AmberDark
import com.example.citygrid.ui.theme.AmberLight
import com.example.citygrid.ui.theme.StatusBlue
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusRed

/**
 * Badge de estado de sensor/sistema — CityGrid v2.
 *
 * Reconoce los estados estándar en mayúsculas/minúsculas y pinta con el color
 * semántico de la paleta:
 *  - LLENO / CRÍTICO / DESCONECTADO  → StatusRed
 *  - MEDIO / ADVERTENCIA             → AmberAccent
 *  - VACÍO / NORMAL / OPERANDO       → StatusGreen
 *  - INFORMACIÓN / AUTO              → StatusBlue
 */
@Composable
fun StatusBadge(
    estado: String,
    modifier: Modifier = Modifier
) {
    val normalized = estado.trim().uppercase()
        .replace("Í", "I").replace("Ó", "O").replace("É", "E")

    val (bgColor, textColor) = when (normalized) {
        "LLENO", "CRITICO", "DESCONECTADO", "INACTIVO" ->
            StatusRed.copy(alpha = 0.12f) to StatusRed
        "MEDIO", "ADVERTENCIA" ->
            AmberLight to AmberDark
        "VACIO", "NORMAL", "OPERANDO", "ACTIVO" ->
            StatusGreen.copy(alpha = 0.12f) to StatusGreen
        "INFORMACION", "AUTO" ->
            StatusBlue.copy(alpha = 0.12f) to StatusBlue
        else ->
            Color.LightGray.copy(alpha = 0.25f) to Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = estado.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}
