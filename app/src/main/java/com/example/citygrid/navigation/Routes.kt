package com.example.citygrid.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Residuos : Screen("residuos")
    object Agua : Screen("agua")
    object Alumbrado : Screen("alumbrado")
    object Alertas : Screen("alertas")
    object Mantenimiento : Screen ("mantenimiento")
}