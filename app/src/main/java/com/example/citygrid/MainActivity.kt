package com.example.citygrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.citygrid.data.SessionManager
import com.example.citygrid.navigation.NavGraph
import com.example.citygrid.navigation.Screen
import com.example.citygrid.ui.components.BarraNavegacionInferiorCompartida
import com.example.citygrid.ui.components.BloqueEncabezado
import com.example.citygrid.ui.theme.CityGridTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val onLogin = currentRoute == Screen.Login.route
            var isDarkTheme by remember { mutableStateOf(sessionManager.isTemaOscuro()) }

            CityGridTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (!onLogin) {
                            BloqueEncabezado(
                                onLogoutClick = {
                                    sessionManager.cerrarSesion()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = {
                                    isDarkTheme = !isDarkTheme
                                    sessionManager.setTemaOscuro(isDarkTheme)
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (!onLogin) {
                            BarraNavegacionInferiorCompartida(
                                navController = navController,
                                currentRoute = currentRoute
                            )
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        sessionManager = sessionManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
