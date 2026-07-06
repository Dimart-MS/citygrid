package com.example.citygrid.data.repository

import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.model.db.DbBomba
import com.example.citygrid.model.db.DbLecturaAgua
import com.example.citygrid.model.db.DbTanque
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.annotations.SupabaseExperimental
import kotlinx.coroutines.flow.Flow

object AguaRepository {
    suspend fun obtenerTanques(): List<DbTanque> {
        return try {
            SupabaseManager.client.from("tanques").select().decodeList<DbTanque>()
        } catch (e: Exception) {
            android.util.Log.e("AguaRepository", "Error al obtener tanques", e)
            emptyList()
        }
    }

    suspend fun obtenerLecturasPorTanque(idTanque: Int): List<DbLecturaAgua> {
        return try {
            SupabaseManager.client
                .from("lecturasagua")
                .select {
                    filter { eq("idtanque", idTanque) }
                }
                .decodeList<DbLecturaAgua>()
        } catch (e: Exception) {
            android.util.Log.e("AguaRepository", "Error al obtener lecturas de agua", e)
            emptyList()
        }
    }

    suspend fun obtenerBombas(): List<DbBomba> {
        return try {
            SupabaseManager.client.from("bombas").select().decodeList<DbBomba>()
        } catch (e: Exception) {
            android.util.Log.e("AguaRepository", "Error al obtener bombas", e)
            emptyList()
        }
    }

    suspend fun obtenerEstadoBomba(idBomba: Int): DbBomba? {
        return try {
            SupabaseManager.client
                .from("bombas")
                .select {
                    filter { eq("idbomba", idBomba) }
                }
                .decodeSingleOrNull<DbBomba>()
        } catch (e: Exception) {
            android.util.Log.e("AguaRepository", "Error al obtener estado bomba", e)
            null
        }
    }

    suspend fun insertarLecturaAgua(idTanque: Int, distanciaCm: Double, nivelAgua: Double): Result<Unit> {
        return try {
            val lectura = DbLecturaAgua(
                idTanque = idTanque,
                distanciaCm = distanciaCm,
                nivelAgua = nivelAgua,
                fechaHora = java.time.OffsetDateTime.now().toString()
            )
            SupabaseManager.client.from("lecturasagua").insert(lectura)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AguaRepository", "Error al insertar lectura de agua", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerUltimasLecturasAgua(idTanque: Int, limite: Int = 50): List<DbLecturaAgua> {
        return try {
            SupabaseManager.client
                .from("lecturasagua")
                .select {
                    filter { eq("idtanque", idTanque) }
                    order("fechahora", Order.DESCENDING)
                    limit(limite.toLong())
                }
                .decodeList<DbLecturaAgua>()
        } catch (e: Exception) {
            android.util.Log.e("AguaRepository", "Error al obtener últimas lecturas de agua", e)
            emptyList()
        }
    }

    @OptIn(SupabaseExperimental::class)
    fun escucharLecturasAgua(): Flow<List<DbLecturaAgua>> {
        return SupabaseManager.client
            .from("lecturasagua")
            .selectAsFlow(DbLecturaAgua::idLecturaAgua)
    }
}
