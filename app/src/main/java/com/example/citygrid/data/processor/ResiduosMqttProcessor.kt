package com.example.citygrid.data.processor

import android.content.Context
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.data.repository.ResiduosRepository
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta
import com.example.citygrid.utils.AlertaHelper
import com.example.citygrid.utils.Constants
import com.example.citygrid.utils.Logger
import com.example.citygrid.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object ResiduosMqttProcessor {

    fun cargarLecturasIniciales(scope: CoroutineScope) {
        scope.launch {
            try {
                val ultimaP = ResiduosRepository.obtenerUltimaLectura(1)
                val ultimaI = ResiduosRepository.obtenerUltimaLectura(2)
                val ultimaO = ResiduosRepository.obtenerUltimaLectura(3)

                val state = MqttManager.residuosFlow.value
                val nuevosContenedores = state.contenedores.map { contenedor ->
                    when (contenedor.tipo.lowercase()) {
                        "plastico" -> {
                            val pct = ultimaP?.nivelLlenado?.toInt() ?: 10
                            contenedor.copy(porcentaje = pct, ultimaActualizacion = System.currentTimeMillis())
                        }
                        "inorganico" -> {
                            val pct = ultimaI?.nivelLlenado?.toInt() ?: 10
                            contenedor.copy(porcentaje = pct, ultimaActualizacion = System.currentTimeMillis())
                        }
                        "organico" -> {
                            val pct = ultimaO?.nivelLlenado?.toInt() ?: 10
                            contenedor.copy(porcentaje = pct, ultimaActualizacion = System.currentTimeMillis())
                        }
                        else -> contenedor
                    }
                }
                MqttManager.updateResiduosState(state.copy(contenedores = nuevosContenedores))
                Logger.d("ResiduosMqttProcessor", "Valores iniciales de botes de basura cargados desde Supabase")
            } catch (e: Exception) {
                Logger.e("ResiduosMqttProcessor", "Error al cargar lecturas iniciales", e)
            }
        }
    }

    fun procesarMensaje(tipo: String, payload: String, context: Context, scope: CoroutineScope, distanciaCm: Double? = null) {
        val pctValue = payload.toIntOrNull()
        if (pctValue == null) {
            Logger.w("ResiduosMqttProcessor", "Payload de residuo no numérico recibido: $payload")
            return
        }

        val porcentaje = pctValue.coerceIn(0, 100)
        actualizarContenedorEnFlow(tipo, porcentaje, context, scope, distanciaCm)
    }

    private fun actualizarContenedorEnFlow(
        tipo: String,
        porcentaje: Int,
        context: Context,
        scope: CoroutineScope,
        distanciaCm: Double? = null
    ) {
        val state = MqttManager.residuosFlow.value
        val contenedoresActuales = state.contenedores.map { contenedor ->
            if (contenedor.tipo.equals(tipo, ignoreCase = true)) {
                contenedor.copy(
                    porcentaje = porcentaje,
                    ultimaActualizacion = System.currentTimeMillis()
                )
            } else {
                contenedor
            }
        }
        MqttManager.updateResiduosState(state.copy(contenedores = contenedoresActuales, ultimoMensajeTimestamp = System.currentTimeMillis()))

        // Registrar la lectura de telemetría en Supabase para el historial de residuos
        scope.launch {
            try {
                val idContenedor = when (tipo.lowercase()) {
                    "plastico" -> 1
                    "inorganico" -> 2
                    "organico" -> 3
                    else -> 1
                }
                val distReal = distanciaCm ?: ((100.0 - porcentaje) * 20.0 / 100.0)
                ResiduosRepository.insertarLectura(idContenedor, distReal, porcentaje.toDouble())
            } catch (e: Exception) {
                Logger.e("ResiduosMqttProcessor", "Error al registrar lectura en segundo plano", e)
            }
        }

        val nombreLimpio = when (tipo.lowercase()) {
            "plastico" -> "Plástico"
            "inorganico" -> "Inorgánico"
            "organico" -> "Orgánico"
            else -> tipo
        }

        val claveAlerta = "${tipo}_critico"
        if (porcentaje > Constants.UMBRAL_CRITICO) {
            scope.launch {
                AlertaHelper.emitirSiNueva(
                    clave = claveAlerta,
                    tipo = TipoAlerta.CRITICO,
                    titulo = "Contenedor $nombreLimpio Crítico",
                    mensaje = "El contenedor de $nombreLimpio ha superado el 85% ($porcentaje%). Requiere vaciado.",
                    dbAlerta = DbAlerta(
                        idSistema = 1,
                        idTipoAlerta = 1,
                        idEstadoAlerta = 1,
                        descripcion = "Contenedor $nombreLimpio al $porcentaje% — requiere vaciado",
                        fechaHora = java.time.OffsetDateTime.now().toString()
                    ),
                    context = context
                )
            }
        } else if (porcentaje < 20) {
            if (AlertaHelper.estaReportada(claveAlerta)) {
                AlertaHelper.liberar(claveAlerta)

                NotificationHelper.enviarNotificacion(
                    context = context,
                    tipo = TipoAlerta.NORMAL,
                    titulo = "Contenedor $nombreLimpio Vaciado",
                    mensaje = "El contenedor de $nombreLimpio ha sido vaciado correctamente (nivel actual: $porcentaje%)."
                )

                scope.launch {
                    val result = AlertaRepository.insertarAlerta(
                        DbAlerta(
                            idSistema = 1,
                            idTipoAlerta = 4,
                            idEstadoAlerta = 2,
                            descripcion = "Contenedor de $nombreLimpio vaciado con éxito (nivel actual: $porcentaje%)",
                            fechaHora = java.time.OffsetDateTime.now().toString()
                        )
                    )
                    if (result.isSuccess) {
                        Logger.d("ResiduosMqttProcessor", "Registro de contenedor vaciado insertado en Supabase")
                    }
                }
            }
        } else {
            if (porcentaje < 75) {
                AlertaHelper.liberar(claveAlerta)
            }
        }
    }
}
