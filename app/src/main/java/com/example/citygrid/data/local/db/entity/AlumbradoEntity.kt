package com.example.citygrid.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alumbrado_cache")
data class AlumbradoEntity(
    @PrimaryKey val id: Int = 1,
    val estadoOn: Boolean,
    val condicionNoche: Boolean,
    val ldrLux: Int,
    val luminariasActivas: Int,
    val modo: String,
    val ultimaActualizacion: Long,
    val conectado: Boolean
)
