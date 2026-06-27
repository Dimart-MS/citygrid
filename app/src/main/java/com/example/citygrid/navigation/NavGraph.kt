package com.example.citygrid.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.citygrid.data.SessionManager
import com.example.citygrid.ui.agua.AguaScreen
import com.example.citygrid.ui.alertas.AlertasScreen
import com.example.citygrid.ui.alumbrado.AlumbradoScreen
import com.example.citygrid.ui.dashboard.DashboardScreen
import com.example.citygrid.ui.login.LoginScreen
import com.example.citygrid.ui.residuos.ResiduosScreen

/**
 * Grafo de navegación de CityGrid.
 *
 * El destino inicial depende de la sesión: si hay sesión activa arranca en el
 * Dashboard, si no, en el Login (auth gate).
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    val startDestination = if (sessionManager.isSesionActiva()) {
        Screen.Dashboard.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ── Login (Módulo 1) ────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ── Dashboard (Módulo 2) ────────────────────────────────────────
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        composable(Screen.Residuos.route) {
            ResiduosScreen()
        }
        composable(Screen.Agua.route) {
            AguaScreen()
        }
        composable(Screen.Alumbrado.route) {
            AlumbradoScreen()
        }
        composable(Screen.Alertas.route) {
            AlertasScreen()
        }
    }
}
