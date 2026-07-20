package com.example.citygrid.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.citygrid.data.local.db.entity.AlumbradoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlumbradoDao {
    @Query("SELECT * FROM alumbrado_cache WHERE id = 1 LIMIT 1")
    fun obtenerAlumbradoFlow(): Flow<AlumbradoEntity?>

    @Query("SELECT * FROM alumbrado_cache WHERE id = 1 LIMIT 1")
    suspend fun obtenerAlumbrado(): AlumbradoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarAlumbrado(alumbrado: AlumbradoEntity)
}
