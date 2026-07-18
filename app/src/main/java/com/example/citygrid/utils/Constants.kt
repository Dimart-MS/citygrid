package com.example.citygrid.utils

object Constants {
    // MQTT Topics — telemetría ESP32 → App
    const val TOPIC_RESIDUOS     = "citygrid/residuos"
    const val TOPIC_AGUA         = "citygrid/agua"
    const val TOPIC_ALUMBRADO    = "citygrid/alumbrado"
    const val TOPIC_ALERTAS      = "citygrid/alertas"
    const val TOPIC_STATUS       = "citygrid/status"

    // MQTT Topics — comandos App → ESP32 (deben coincidir con el sketch)
    const val TOPIC_CONTROL      = "citygrid/control"   // genérico (reservado)
    const val TOPIC_CTRL_LUCES   = "control-luces"      // relay + LEDs exteriores
    const val TOPIC_CTRL_BOMBA   = "control-bomba"      // bomba de agua

    // MQTT HiveMQ Cloud Configuration
    const val MQTT_BROKER_URL    = "ssl://7616ccef7e334086bf74b6bb92340be3.s1.eu.hivemq.cloud:8883"
    const val MQTT_USER          = "CityGrid"
    const val MQTT_PASSWORD      = "CityGridPasswordSec1"
    const val CLIENT_ID          = "CityGrid_Android_Client"

    // Supabase Configuration
    const val SUPABASE_URL       = "https://xdwccycazmjbizwnljxo.supabase.co"
    const val SUPABASE_KEY       = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inhkd2NjeWNhem1qYml6d25sanhvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI0Nzk1OTgsImV4cCI6MjA5ODA1NTU5OH0.wd6KGYO_V_xwDlRk4pPMPuia0Un19XkFHXSEEtCKcUs"

    // Umbrales
    const val UMBRAL_CRITICO     = 75
    const val UMBRAL_MEDIO       = 50
    const val UMBRAL_AGUA_BAJO   = 30
    const val UMBRAL_LUZ_ADC     = 1500

    // Umbrales de residuos (porcentaje)
    const val UMBRAL_RESIDUOS_CRITICO = 85
    const val UMBRAL_RESIDUOS_MEDIO = 50

    // Tiempos
    const val TIEMPO_OVERRIDE_MANUAL_MS = 60000L // 60 segundos
    const val INTERVALO_CHECK_CONEXION_MS = 1000L // 1 segundo
    const val TIMEOUT_CONEXION_MQTT_MS = 2 * 60 * 1000L // 2 minutos
    const val DEBOUNCE_SWITCH_MS = 300L // 300ms para evitar doble-click en switches

    // Límites de listas
    const val MAX_EVENTOS_RECIENTES = 3
    const val MAX_ALERTAS_DASHBOARD = 10
    const val MAX_HISTORIAL_LECTURAS = 50

    // SharedPreferences
    const val PREFS_NAME         = "citygrid_prefs"
    const val KEY_CORREO         = "usuario_correo"
    const val KEY_NOMBRE         = "usuario_nombre"
    const val KEY_ID_USUARIO     = "id_usuario"
    const val KEY_SESION_ACTIVA  = "sesion_activa"
    const val KEY_TEMA_OSCURO    = "tema_oscuro"
    const val KEY_ULTIMA_SYNC    = "ultima_sync"

    // Notificaciones
    const val CHANNEL_ID         = "citygrid_alertas"
    const val CHANNEL_NAME       = "Alertas CityGrid"
}
