package com.example.citygrid.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val iconName: String, // "home", "trash", "water", "sun", "bell"
    val systemIcon: ImageVector? = null
) {
    object Home : BottomNavItem(Screen.Dashboard.route, "Inicio", "home", Icons.Default.Home)
    object Residuos : BottomNavItem(Screen.Residuos.route, "Residuos", "trash", Icons.Default.Delete)
    object Agua : BottomNavItem(Screen.Agua.route, "Agua", "water")
    object Alumbrado : BottomNavItem(Screen.Alumbrado.route, "Alumbrado", "sun")
    object Alertas : BottomNavItem(Screen.Alertas.route, "Alertas", "bell", Icons.Default.Notifications)
}