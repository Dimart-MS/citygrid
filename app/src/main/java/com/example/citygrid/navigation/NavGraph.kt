package com.example.citygrid.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.citygrid.data.SessionManager
import com.example.citygrid.ui.agua.AguaScreen
import com.example.citygrid.ui.alertas.AlertasScreen
import com.example.citygrid.ui.alumbrado.AlumbradoScreen
import com.example.citygrid.ui.configuracion.ConfiguracionScreen
import com.example.citygrid.ui.configuracion.ConfiguracionViewModel
import com.example.citygrid.ui.dashboard.DashboardScreen
import com.example.citygrid.ui.login.LoginScreen
import com.example.citygrid.ui.mantenimiento.MantenimientoScreen
import com.example.citygrid.ui.residuos.ResiduosScreen

/**
 * Grafo de navegación de CityGrid.
 *
 * El destino inicial depende de la sesión: si hay sesión activa arranca en el
 * Dashboard, si no, en el Login (auth gate).
 */
private const val ANIM_DURATION = 300

@Composable
fun NavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier,
    onThemeChanged: () -> Unit = {}
) {
    val startDestination = if (sessionManager.isSesionActiva()) {
        Screen.Dashboard.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(ANIM_DURATION)
            ) + fadeIn(animationSpec = tween(ANIM_DURATION))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(ANIM_DURATION)
            ) + fadeOut(animationSpec = tween(ANIM_DURATION))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(ANIM_DURATION)
            ) + fadeIn(animationSpec = tween(ANIM_DURATION))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(ANIM_DURATION)
            ) + fadeOut(animationSpec = tween(ANIM_DURATION))
        }
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
            ResiduosScreen(
                onNavigateToAlerts = {
                    navController.navigate(Screen.Alertas.route) {
                        launchSingleTop = true
                    }
                }
            )
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
        composable(Screen.Mantenimiento.route) {
            MantenimientoScreen()
        }
        composable(Screen.Configuracion.route) {
            ConfiguracionScreen(
                viewModel = ConfiguracionViewModel(sessionManager),
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onThemeChanged = onThemeChanged
            )
        }
    }
}
