package com.example.citygrid.data.processor

import android.content.Context
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.AguaPayload
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.data.repository.AguaRepository
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta
import com.example.citygrid.utils.Constants
import com.example.citygrid.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

object AguaMqttProcessor {
    private val alertasReportadas = mutableMapOf<String, Boolean>()

    fun procesarMensaje(topic: String, payload: String, scope: CoroutineScope) {
        scope.launch {
            try {
                val data = Json.decodeFromString<AguaPayload>(payload)
                val nivelTanque = data.nivelTanque ?: 50
                val distanciaCm = (100.0 - nivelTanque) * 20.0 / 100.0
                AguaRepository.insertarLecturaAgua(
                    idTanque = 1,
                    distanciaCm = distanciaCm,
                    nivelAgua = nivelTanque.toDouble()
                )
            } catch (e: Exception) {
                android.util.Log.e("AguaMqttProcessor", "Error al procesar mensaje de Agua", e)
            }
        }
    }

    fun verificarAlertasAgua(nivelTanque: Int, context: Context, scope: CoroutineScope) {
        if (nivelTanque < Constants.UMBRAL_AGUA_BAJO) {
            val claveAlerta = "agua_bajo"
            if (alertasReportadas[claveAlerta] != true) {
                alertasReportadas[claveAlerta] = true

                // 1. Mostrar notificación push de inmediato
                NotificationHelper.enviarNotificacion(
                    context = context,
                    tipo = TipoAlerta.ADVERTENCIA,
                    titulo = "Tanque de Agua Bajo",
                    mensaje = "El nivel del tanque de agua es bajo ($nivelTanque%). Se requiere llenado."
                )

                // 2. Registrar la alerta en la Base de Datos (Supabase) en segundo plano
                scope.launch {
                    val dbAlerta = DbAlerta(
                        idSistema = 2, // Módulo de Agua
                        idTipoAlerta = 2, // ADVERTENCIA
                        idEstadoAlerta = 1, // PENDIENTE
                        descripcion = "Nivel del tanque bajo ($nivelTanque%) — requiere llenado",
                        fechaHora = java.time.OffsetDateTime.now().toString()
                    )
                    val result = AlertaRepository.insertarAlerta(dbAlerta)
                    if (result.isSuccess) {
                        android.util.Log.d("AguaMqttProcessor", "Alerta de agua baja insertada en Supabase")
                    }
                }
            }
        } else {
            alertasReportadas["agua_bajo"] = false
        }
    }
}
