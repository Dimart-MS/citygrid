package com.example.citygrid.model

data class ContenedorData(
    val nombre: String = "",
    val tipo: String = "",          // "Plastico", "Inorganico", "Organico"
    val porcentaje: Int = 0,
    val ultimaActualizacion: Long = 0L
)

data class ResiduosState(
    val contenedores: List<ContenedorData> = emptyList(),
    val conectado: Boolean = false
)
