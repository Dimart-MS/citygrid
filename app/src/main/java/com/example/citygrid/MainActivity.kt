package com.example.citygrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.citygrid.navigation.NavGraph
import com.example.citygrid.navigation.Screen
import com.example.citygrid.ui.components.BarraNavegacionInferiorCompartida
import com.example.citygrid.ui.components.BloqueEncabezado
import com.example.citygrid.ui.theme.CityGridTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            CityGridTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // Ocultar encabezado en pantalla de Login si fuera necesario
                        if (currentRoute != Screen.Login.route) {
                            BloqueEncabezado(onNotificationClick = {
                                navController.navigate(Screen.Alertas.route) {
                                    popUpTo(Screen.Dashboard.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            })
                        }
                    },
                    bottomBar = {
                        if (currentRoute != Screen.Login.route) {
                            BarraNavegacionInferiorCompartida(navController = navController, currentRoute = currentRoute)
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}