package com.example.citygrid.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.citygrid.data.local.db.entity.ResiduosEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResiduosDao {
    @Query("SELECT * FROM residuos_cache")
    fun obtenerTodosFlow(): Flow<List<ResiduosEntity>>

    @Query("SELECT * FROM residuos_cache")
    suspend fun obtenerTodosList(): List<ResiduosEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(residuos: List<ResiduosEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(residuo: ResiduosEntity)
}
