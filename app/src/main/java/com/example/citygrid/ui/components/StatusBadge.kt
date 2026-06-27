package com.example.citygrid.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.citygrid.ui.theme.StatusBlue
import com.example.citygrid.ui.theme.StatusGreen
import com.example.citygrid.ui.theme.StatusRed
import com.example.citygrid.ui.theme.StatusYellow

/**
 * Badge de estado de sensor/sistema.
 *
 * Reconoce los estados estándar de CityGrid (en mayúsculas o minúsculas, con o sin
 * acentos) y pinta con el color semántico correspondiente de la paleta:
 *  - LLENO / CRÍTICO         -> StatusRed
 *  - MEDIO / ADVERTENCIA     -> StatusYellow
 *  - VACÍO / NORMAL          -> StatusGreen
 *  - INFORMACIÓN / INFORMACION -> StatusBlue
 *  - cualquier otro          -> gris
 */
@Composable
fun StatusBadge(
    estado: String,
    modifier: Modifier = Modifier
) {
    val normalized = estado.trim().uppercase()
        .replace("Í", "I").replace("Ó", "O")

    val (bgColor, textColor) = when (normalized) {
        "LLENO", "CRITICO"          -> StatusRed    to Color.White
        "MEDIO", "ADVERTENCIA"      -> StatusYellow to Color.Black
        "VACIO", "NORMAL"           -> StatusGreen  to Color.White
        "INFORMACION"               -> StatusBlue   to Color.White
        else                        -> Color(0xFF9E9E9E) to Color.White
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = estado,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
