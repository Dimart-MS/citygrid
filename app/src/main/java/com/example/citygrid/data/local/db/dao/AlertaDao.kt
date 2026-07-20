package com.example.citygrid.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.citygrid.data.local.db.entity.AlertaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertaDao {
    @Query("SELECT * FROM alertas_cache ORDER BY timestamp DESC")
    fun obtenerAlertasFlow(): Flow<List<AlertaEntity>>

    @Query("SELECT * FROM alertas_cache ORDER BY timestamp DESC LIMIT 50")
    suspend fun obtenerAlertas(): List<AlertaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarAlerta(alerta: AlertaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarAlertas(alertas: List<AlertaEntity>)
}

