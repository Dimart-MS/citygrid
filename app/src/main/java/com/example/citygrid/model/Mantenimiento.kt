package com.example.citygrid.model

data class Mantenimiento(
    val id: String = "",
    val dispositivo: String = "",
    val tipo: String = "",          // "Preventivo" / "Correctivo"
    val fecha: Long = 0L,
    val responsable: String = "",
    val descripcion: String = ""
)