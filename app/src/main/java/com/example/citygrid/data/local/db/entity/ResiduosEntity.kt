package com.example.citygrid.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "residuos_cache")
data class ResiduosEntity(
    @PrimaryKey val tipo: String,
    val nombre: String,
    val porcentaje: Int,
    val ultimaActualizacion: Long
)
