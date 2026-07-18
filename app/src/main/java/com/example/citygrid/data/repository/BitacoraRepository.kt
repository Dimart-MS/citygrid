package com.example.citygrid.data.repository

import com.example.citygrid.utils.Logger
import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.model.db.DbBitacoraSistema
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Repositorio para registrar acciones del usuario (login, cierre de sesión, mantenimiento,
 * marcar alertas) en la tabla bitacorasistema de Supabase.
 *
 * Todas las funciones son fire-and-forget: se llaman desde un scope existente
 * y el error se loguea sin propagar excepciones al caller.
 */
object BitacoraRepository {

    private const val TAG = "BitacoraRepository"

    /**
     * Registra una acción en la bitácora del sistema.
     *
     * @param idUsuario ID del usuario que realiza la acción (0 si es anónimo/sistema).
     * @param accion    Código/nombre corto de la acción (ej. "LOGIN", "MANTENIMIENTO_REGISTRO").
     * @param descripcion Descripción opcional de detalle.
     */
    suspend fun registrar(
        idUsuario: Int,
        accion: String,
        descripcion: String? = null
    ) {
        try {
            val registro = DbBitacoraSistema(
                idUsuario = idUsuario,
                accion = accion,
                descripcion = descripcion,
                fechaHora = OffsetDateTime.now(ZoneOffset.UTC).toString()
            )
            SupabaseManager.client
                .from("bitacorasistema")
                .insert(registro)
            Logger.d(TAG, "Bitácora registrada: [$accion] usuario=$idUsuario")
        } catch (e: Exception) {
            Logger.e(TAG, "Error al registrar en bitácora: $accion", e)
        }
    }

    /** Carga las últimas N entradas de la bitácora para consulta. */
    suspend fun obtenerUltimas(limite: Int = 50): List<DbBitacoraSistema> {
        return try {
            SupabaseManager.client
                .from("bitacorasistema")
                .select {
                    order("fechahora", Order.DESCENDING)
                    limit(limite.toLong())
                }
                .decodeList<DbBitacoraSistema>()
        } catch (e: Exception) {
            Logger.e(TAG, "Error al cargar bitácora", e)
            emptyList()
        }
    }
}
