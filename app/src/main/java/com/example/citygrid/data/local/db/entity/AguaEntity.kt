package com.example.citygrid.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agua_cache")
data class AguaEntity(
    @PrimaryKey val id: Int = 1,
    val nivelTanque: Int,
    val bombaActiva: Boolean,
    val estadoGeneral: String,
    val ultimaActualizacion: Long,
    val conectado: Boolean
)
