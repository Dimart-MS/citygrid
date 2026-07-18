package com.example.citygrid.data.processor

import android.content.Context
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.AguaPayload
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.data.repository.AguaRepository
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta
import com.example.citygrid.utils.AlertaHelper
import com.example.citygrid.utils.Constants
import com.example.citygrid.utils.Logger
import com.example.citygrid.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

object AguaMqttProcessor {
    private val json = Json { ignoreUnknownKeys = true }

    fun procesarMensaje(topic: String, payload: String, context: Context, scope: CoroutineScope) {
        scope.launch {
            try {
                val data = json.decodeFromString<AguaPayload>(payload)
                val nivelTanque = data.nivelTanque ?: 50
                val bombaActiva = data.bombaActiva ?: (nivelTanque < Constants.UMBRAL_AGUA_BAJO)

                MqttManager.updateAguaState(
                    MqttManager.aguaFlow.value.copy(
                        nivelTanque = nivelTanque,
                        bombaActiva = bombaActiva,
                        estadoGeneral = if (nivelTanque >= Constants.UMBRAL_AGUA_BAJO) "Operando correctamente" else "Nivel Crítico",
                        ultimaActualizacion = System.currentTimeMillis()
                    )
                )

                verificarAlertasAgua(nivelTanque, context, this)

                val distanciaCm = (100.0 - nivelTanque) * 20.0 / 100.0
                AguaRepository.insertarLecturaAgua(
                    idTanque = 1,
                    distanciaCm = distanciaCm,
                    nivelAgua = nivelTanque.toDouble()
                )
            } catch (e: Exception) {
                Logger.e("AguaMqttProcessor", "Error al procesar mensaje de Agua", e)
            }
        }
    }

    fun verificarAlertasAgua(nivelTanque: Int, context: Context, scope: CoroutineScope) {
        val claveAlerta = "agua_bajo"
        if (nivelTanque < Constants.UMBRAL_AGUA_BAJO) {
            scope.launch {
                AlertaHelper.emitirSiNueva(
                    clave = claveAlerta,
                    tipo = TipoAlerta.ADVERTENCIA,
                    titulo = "Tanque de Agua Bajo",
                    mensaje = "El nivel del tanque de agua es bajo ($nivelTanque%). Se requiere llenado.",
                    dbAlerta = DbAlerta(
                        idSistema = 2,
                        idTipoAlerta = 2,
                        idEstadoAlerta = 1,
                        descripcion = "Nivel del tanque bajo ($nivelTanque%) — requiere llenado",
                        fechaHora = java.time.OffsetDateTime.now().toString()
                    ),
                    context = context
                )
            }
        } else {
            AlertaHelper.liberar(claveAlerta)
        }
    }
}
