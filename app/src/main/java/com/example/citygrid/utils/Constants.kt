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