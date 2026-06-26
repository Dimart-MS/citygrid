package com.example.citygrid.model

data class AguaState(
    val nivelTanque: Int = 0,
    val bombaActiva: Boolean = false,
    val estadoGeneral: String = "Operando",
    val ultimaActualizacion: Long = 0L,
    val conectado: Boolean = false
)