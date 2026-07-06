package com.example.citygrid.model

import kotlinx.serialization.Serializable

@Serializable
data class StatusState(
    val estado: String = "ONLINE",
    val uptime: Long = 0,
    val rssi: Int = 0,
    val sensores: SensorHealth = SensorHealth()
)

@Serializable
data class SensorHealth(
    val basPlastico: String = "OK",
    val basInorganico: String = "OK",
    val basOrganico: String = "OK",
    val nivelAgua: String = "OK",
    val ldrLuz: String = "OK"
)
