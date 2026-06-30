package com.example.citygrid.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.citygrid.R
import com.example.citygrid.ui.theme.CityGridPrimary
import com.example.citygrid.ui.theme.SurfaceCard

/**
 * TopBar reutilizable de CityGrid.
 *
 * Muestra el logo (R.drawable.logosinfondo) con un fallback a [Icons.Default.GridOn]
 * por si el recurso no existe aún, el nombre de la app y una campana de notificaciones
 * con un contador opcional ([badgeCount]).
 */
@Composable
fun CityGridTopBar(
    onBellClick: () -> Unit = {},
    badgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // El logo "logosinfondo" debe existir en res/drawable-nodpi/logosinfondo.png
    val logoExists = runCatching {
        context.resources.getIdentifier("logosinfondo", "drawable", context.packageName)
    }.getOrDefault(0) != 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = CityGridGreenDarkTheme())
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (logoExists) {
                Image(
                    painter = painterResource(id = R.drawable.logosinfondo),
                    contentDescription = "Logo CityGrid",
                    modifier = Modifier.size(45.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.GridOn,
                    contentDescription = "Logo CityGrid",
                    tint = SurfaceCard,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = "CityGrid",
                    color = SurfaceCard,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Smart City Platform",
                    color = SurfaceCard.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.weight(1f))

            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge { Text(text = badgeCount.toString()) }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceCard, CircleShape)
                        .clickable { onBellClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Alertas",
                        tint = CityGridPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/** Color del header según el tema (verde oscuro CityGrid en claro, verde oscuro en dark). */
@Composable
private fun CityGridGreenDarkTheme(): Color =
    MaterialTheme.colorScheme.primaryContainer
