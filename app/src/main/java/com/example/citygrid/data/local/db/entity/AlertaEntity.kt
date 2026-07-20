package com.example.citygrid.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alertas_cache")
data class AlertaEntity(
    @PrimaryKey val id: String,
    val tipoName: String,
    val titulo: String,
    val descripcion: String,
    val sistema: String,
    val timestamp: Long,
    val atendida: Boolean
)
