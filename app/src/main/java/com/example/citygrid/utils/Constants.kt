package com.example.citygrid.utils

object Constants {
    // MQTT
    const val MQTT_BROKER        = "tcp://broker.hivemq.com:1883"
    const val TOPIC_RESIDUOS     = "citygrid/residuos"
    const val TOPIC_AGUA         = "citygrid/agua"
    const val TOPIC_ALUMBRADO    = "citygrid/alumbrado"
    const val TOPIC_ALERTAS      = "citygrid/alertas"
    const val TOPIC_CONTROL      = "citygrid/control"

    // Umbrales
    const val UMBRAL_CRITICO     = 85
    const val UMBRAL_MEDIO       = 50
    const val UMBRAL_AGUA_BAJO   = 30

    // SharedPreferences
    const val PREFS_NAME         = "citygrid_prefs"
    const val KEY_CORREO         = "usuario_correo"
    const val KEY_NOMBRE         = "usuario_nombre"
    const val KEY_SESION_ACTIVA  = "sesion_activa"
    const val KEY_TEMA_OSCURO    = "tema_oscuro"
    const val KEY_ULTIMA_SYNC    = "ultima_sync"

    // Notificaciones
    const val CHANNEL_ID         = "citygrid_alertas"
    const val CHANNEL_NAME       = "Alertas CityGrid"
}