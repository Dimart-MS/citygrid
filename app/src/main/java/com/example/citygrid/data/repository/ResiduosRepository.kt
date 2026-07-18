package com.example.citygrid.data.repository

import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.model.db.DbContenedor
import com.example.citygrid.model.db.DbLecturaResiduo
import com.example.citygrid.utils.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.annotations.SupabaseExperimental
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object ResiduosRepository {
    suspend fun obtenerContenedores(): List<DbContenedor> {
        return try {
            SupabaseManager.client.from("contenedores").select().decodeList<DbContenedor>()
        } catch (e: Exception) {
            Logger.e("ResiduosRepository", "Error al obtener contenedores", e)
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
            Logger.e("ResiduosRepository", "Error al obtener lecturas", e)
            emptyList()
        }
    }

    suspend fun insertarLectura(idContenedor: Int, distanciaCm: Double, nivelLlenado: Double): Result<Unit> {
        return try {
            val lectura = DbLecturaResiduo(
                idContenedor = idContenedor,
                distanciaCm = distanciaCm,
                nivelLlenado = nivelLlenado,
                fechaHora = java.time.OffsetDateTime.now().toString()
            )
            SupabaseManager.client.from("lecturasresiduos").insert(lectura)
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("ResiduosRepository", "Error al insertar lectura de residuo", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerUltimaLectura(idContenedor: Int): DbLecturaResiduo? {
        return try {
            SupabaseManager.client
                .from("lecturasresiduos")
                .select {
                    filter { eq("idcontenedor", idContenedor) }
                    order("idlecturaresiduo", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<DbLecturaResiduo>()
        } catch (e: Exception) {
            Logger.e("ResiduosRepository", "Error al obtener última lectura de residuo", e)
            null
        }
    }

    @OptIn(SupabaseExperimental::class)
    fun escucharLecturasResiduos(): Flow<List<DbLecturaResiduo>> {
        return SupabaseManager.client
            .from("lecturasresiduos")
            .selectAsFlow(DbLecturaResiduo::idLecturaResiduo)
    }
}
