package com.example.citygrid.data.repository

import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.model.db.DbLecturaLuminaria
import com.example.citygrid.model.db.DbLuminaria
import com.example.citygrid.utils.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.annotations.SupabaseExperimental
import kotlinx.coroutines.flow.Flow

object AlumbradoRepository {
    suspend fun obtenerLuminarias(): List<DbLuminaria> {
        return try {
            SupabaseManager.client.from("luminarias").select().decodeList<DbLuminaria>()
        } catch (e: Exception) {
            Logger.e("AlumbradoRepository", "Error al obtener luminarias", e)
            emptyList()
        }
    }

    suspend fun obtenerLecturasPorLuminaria(idLuminaria: Int): List<DbLecturaLuminaria> {
        return try {
            SupabaseManager.client
                .from("lecturasluminaria")
                .select {
                    filter { eq("idluminaria", idLuminaria) }
                }
                .decodeList<DbLecturaLuminaria>()
        } catch (e: Exception) {
            Logger.e("AlumbradoRepository", "Error al obtener lecturas luminaria", e)
            emptyList()
        }
    }

    suspend fun insertarLecturaLuminaria(idLuminaria: Int, valorLdr: Int): Result<Unit> {
        return try {
            val lectura = DbLecturaLuminaria(
                idLuminaria = idLuminaria,
                valorLdr = valorLdr,
                fechaHora = java.time.OffsetDateTime.now().toString()
            )
            SupabaseManager.client.from("lecturasluminaria").insert(lectura)
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("AlumbradoRepository", "Error al insertar lectura de alumbrado", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerUltimasLecturasLuminaria(idLuminaria: Int, limite: Int = 50): List<DbLecturaLuminaria> {
        return try {
            SupabaseManager.client
                .from("lecturasluminaria")
                .select {
                    filter { eq("idluminaria", idLuminaria) }
                    order("fechahora", Order.DESCENDING)
                    limit(limite.toLong())
                }
                .decodeList<DbLecturaLuminaria>()
        } catch (e: Exception) {
            Logger.e("AlumbradoRepository", "Error al obtener últimas lecturas de luminaria", e)
            emptyList()
        }
    }

    @OptIn(SupabaseExperimental::class)
    fun escucharLecturasLuminarias(): Flow<List<DbLecturaLuminaria>> {
        return SupabaseManager.client
            .from("lecturasluminaria")
            .selectAsFlow(DbLecturaLuminaria::idLecturaLuminaria)
    }
}
