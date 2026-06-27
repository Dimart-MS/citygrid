package com.example.citygrid.data.repository

import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.model.db.DbBomba
import com.example.citygrid.model.db.DbLecturaAgua
import com.example.citygrid.model.db.DbTanque
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun escucharLecturasAgua(): Flow<DbLecturaAgua> {
        return emptyFlow()
    }
}
