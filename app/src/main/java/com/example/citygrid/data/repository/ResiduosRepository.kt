package com.example.citygrid.data.repository

import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.model.db.DbContenedor
import com.example.citygrid.model.db.DbLecturaResiduo
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object ResiduosRepository {
    suspend fun obtenerContenedores(): List<DbContenedor> {
        return try {
            SupabaseManager.client.from("contenedores").select().decodeList<DbContenedor>()
        } catch (e: Exception) {
            android.util.Log.e("ResiduosRepository", "Error al obtener contenedores", e)
            emptyList()
        }
    }

    suspend fun obtenerLecturasPorContenedor(idContenedor: Int): List<DbLecturaResiduo> {
        return try {
            SupabaseManager.client
                .from("lecturasresiduos")
                .select {
                    filter { eq("idcontenedor", idContenedor) }
                }
                .decodeList<DbLecturaResiduo>()
        } catch (e: Exception) {
            android.util.Log.e("ResiduosRepository", "Error al obtener lecturas", e)
            emptyList()
        }
    }

    suspend fun insertarLectura(idContenedor: Int, distanciaCm: Double, nivelLlenado: Double): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun escucharLecturasResiduos(): Flow<DbLecturaResiduo> {
        return emptyFlow()
    }
}
