package com.example.citygrid.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.citygrid.data.local.db.entity.AguaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AguaDao {
    @Query("SELECT * FROM agua_cache WHERE id = 1 LIMIT 1")
    fun obtenerAguaFlow(): Flow<AguaEntity?>

    @Query("SELECT * FROM agua_cache WHERE id = 1 LIMIT 1")
    suspend fun obtenerAgua(): AguaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarAgua(agua: AguaEntity)
}
