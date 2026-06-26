package com.example.citygrid.model

data class AlumbradoState(
    val estadoOn: Boolean = false,
    val condicionNoche: Boolean = false,
    val ldrLux: Int = 0,
    val luminariasActivas: Int = 0,
    val modo: String = "AUTO",
    val ultimaActualizacion: Long = 0L,
    val conectado: Boolean = false
)