package com.example.citygrid.model

data class Contenedor(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "",           // "Plastico", "Inorganico", "Organico"
    val ubicacion: String = "",
    val porcentaje: Int = 0,
    val activo: Boolean = true,
    val ultimaActualizacion: Long = 0L
)