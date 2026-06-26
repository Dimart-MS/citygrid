package com.example.citygrid.model

data class Alerta(
    val id: String = "",
    val tipo: TipoAlerta = TipoAlerta.INFORMACION,
    val titulo: String = "",
    val descripcion: String = "",
    val sistema: String = "",       // "Residuos", "Agua", "Alumbrado", "Sistema"
    val timestamp: Long = 0L,
    val atendida: Boolean = false
)

enum class TipoAlerta { CRITICO, ADVERTENCIA, INFORMACION, NORMAL }