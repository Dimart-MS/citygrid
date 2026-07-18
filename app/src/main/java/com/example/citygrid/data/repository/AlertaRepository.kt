package com.example.citygrid.data.repository

import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.model.db.DbAlerta
import com.example.citygrid.model.db.DbNotificacion
import com.example.citygrid.utils.Logger
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.flow.Flow

@OptIn(SupabaseExperimental::class)
object AlertaRepository {
    /**
     * Obtiene todas las alertas de la tabla 'alertas'.
     */
    suspend fun obtenerAlertas(): List<DbAlerta> {
        return try {
            SupabaseManager.client
                .from("alertas")
                .select()
                .decodeList<DbAlerta>()
        } catch (e: Exception) {
            Logger.e("AlertaRepository", "Error al obtener alertas por REST", e)
            emptyList()
        }
    }

    /**
     * Escucha alertas creadas en tiempo real desde Supabase Realtime.
     */
    fun escucharAlertasRealtime(): Flow<List<DbAlerta>> {
        return SupabaseManager.client
            .from("alertas")
            .selectAsFlow(DbAlerta::idAlerta)
    }

    /**
     * Obtiene notificaciones de la tabla 'notificaciones'.
     */
    suspend fun obtenerNotificaciones(): List<DbNotificacion> {
        return try {
            SupabaseManager.client
                .from("notificaciones")
                .select()
                .decodeList<DbNotificacion>()
        } catch (e: Exception) {
            Logger.e("AlertaRepository", "Error al obtener notificaciones por REST", e)
            emptyList()
        }
    }

    /**
     * Marca una alerta como ATENDIDA (actualiza IdEstadoAlerta a 2).
     */
    suspend fun marcarAlertaComoAtendida(idAlerta: Long): Result<Unit> {
        return try {
            SupabaseManager.client
                .from("alertas")
                .update(
                    {
                        set("idestadoalerta", 2)
                    }
                ) {
                    filter {
                        eq("idalerta", idAlerta)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inserta una nueva alerta en la tabla 'alertas' de Supabase.
     */
    suspend fun insertarAlerta(dbAlerta: DbAlerta): Result<Unit> {
        return try {
            SupabaseManager.client
                .from("alertas")
                .insert(dbAlerta)
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("AlertaRepository", "Error al insertar alerta", e)
            Result.failure(e)
        }
    }

    /**
     * Escucha nuevas notificaciones en tiempo real desde Supabase Realtime.
     */
    fun escucharNotificacionesRealtime(): Flow<List<DbNotificacion>> {
        return SupabaseManager.client
            .from("notificaciones")
            .selectAsFlow(DbNotificacion::idNotificacion)
    }
}
