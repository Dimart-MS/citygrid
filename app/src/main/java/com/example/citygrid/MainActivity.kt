package com.example.citygrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.citygrid.data.SessionManager
import com.example.citygrid.navigation.NavGraph
import com.example.citygrid.navigation.Screen
import com.example.citygrid.ui.components.BarraNavegacionInferiorCompartida
import com.example.citygrid.ui.components.BloqueEncabezado
import android.os.Build
import com.example.citygrid.ui.theme.CityGridTheme
import com.example.citygrid.utils.NotificationHelper
import com.example.citygrid.data.MqttManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)

        // Inicializar canal de notificaciones y solicitar permiso en Android 13+ (Requisito del examen)
        NotificationHelper.createNotificationChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Conectar cliente MQTT en segundo plano al iniciar
        MqttManager.connect(applicationContext)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val onLogin = currentRoute == Screen.Login.route

            val alertasCount by MqttManager.alertasFlow.collectAsState()
            val alertasPendientes = alertasCount.count { !it.atendida }

            val connectionState by MqttManager.connectionState.collectAsState()
            val latenciaMs by MqttManager.latenciaMs.collectAsState()

            CityGridTheme(darkTheme = sessionManager.isTemaOscuro()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        if (!onLogin) {
                            BloqueEncabezado(
                                alertasPendientes = alertasPendientes,
                                connectionState = connectionState,
                                latenciaMs = latenciaMs,
                                onLogoutClick = {
                                    sessionManager.cerrarSesion()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onBellClick = {
                                    navController.navigate(Screen.Alertas.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onConfigClick = {
                                    navController.navigate(Screen.Configuracion.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
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
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                        onThemeChanged = { recreate() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        MqttManager.shutdown()
        super.onDestroy()
    }
}
