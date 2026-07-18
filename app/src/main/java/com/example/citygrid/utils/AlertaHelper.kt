package com.example.citygrid.utils

import android.content.Context
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta

/**
 * Helper para emitir alertas con deduplicación (evita notificaciones repetidas).
 * Reemplaza los mutableMapOf<String, Boolean> duplicados en cada procesador MQTT.
 */
object AlertaHelper {
    private val alertasReportadas = mutableMapOf<String, Boolean>()

    /**
     * Emite notificación + inserta en BD solo si la alerta no ha sido reportada antes.
     * @return true si la alerta fue emitida, false si ya había sido reportada.
     */
    suspend fun emitirSiNueva(
        clave: String,
        tipo: TipoAlerta,
        titulo: String,
        mensaje: String,
        dbAlerta: DbAlerta,
        context: Context
    ): Boolean {
        if (alertasReportadas[clave] == true) return false
        alertasReportadas[clave] = true

        NotificationHelper.enviarNotificacion(context, tipo, titulo, mensaje)

        val result = AlertaRepository.insertarAlerta(dbAlerta)
        if (result.isSuccess) {
            Logger.d("AlertaHelper", "Alerta '$clave' insertada en Supabase")
        }
        return true
    }

    fun estaReportada(clave: String): Boolean = alertasReportadas[clave] == true

    fun liberar(clave: String) {
        alertasReportadas[clave] = false
    }

    /** Limpia todas las banderas — útil en reinicio de conexión. */
    fun limpiarTodas() {
        alertasReportadas.clear()
    }
}
