package com.example.citygrid.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.citygrid.ui.agua.AguaScreen
import com.example.citygrid.ui.alertas.AlertasScreen
import com.example.citygrid.ui.alumbrado.AlumbradoScreen
import com.example.citygrid.ui.dashboard.DashboardScreen
import com.example.citygrid.ui.residuos.ResiduosScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen()
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