package com.example.citygrid.data.processor

import android.content.Context
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.data.repository.ResiduosRepository
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta
import com.example.citygrid.utils.Constants
import com.example.citygrid.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object ResiduosMqttProcessor {
    private val alertasReportadas = mutableMapOf<String, Boolean>()

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
                android.util.Log.d("ResiduosMqttProcessor", "Valores iniciales de botes de basura cargados desde Supabase")
            } catch (e: Exception) {
                android.util.Log.e("ResiduosMqttProcessor", "Error al cargar lecturas iniciales", e)
            }
        }
    }

    fun procesarMensaje(tipo: String, payload: String, context: Context, scope: CoroutineScope, distanciaCm: Double? = null) {
        val pctValue = payload.toIntOrNull()
        if (pctValue == null) {
            android.util.Log.w("ResiduosMqttProcessor", "Payload de residuo no numérico recibido: $payload")
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

        // 3. Registrar la lectura de telemetría en Supabase para el historial de residuos
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
                android.util.Log.e("ResiduosMqttProcessor", "Error al registrar lectura en segundo plano", e)
            }
        }

        // Verificar umbral del 85% para activar alerta crítica (Requisito del examen)
        val nombreLimpio = when (tipo.lowercase()) {
            "plastico" -> "Plástico"
            "inorganico" -> "Inorgánico"
            "organico" -> "Orgánico"
            else -> tipo
        }

        val claveAlerta = "${tipo}_critico"
        if (porcentaje > Constants.UMBRAL_CRITICO) {
            if (alertasReportadas[claveAlerta] != true) {
                alertasReportadas[claveAlerta] = true

                // 1. Mostrar notificación push de inmediato
                NotificationHelper.enviarNotificacion(
                    context = context,
                    tipo = TipoAlerta.CRITICO,
                    titulo = "Contenedor $nombreLimpio Crítico",
                    mensaje = "El contenedor de $nombreLimpio ha superado el 85% ($porcentaje%). Requiere vaciado."
                )

                // 2. Registrar la alerta crítica en la Base de Datos (Supabase) en segundo plano
                scope.launch {
                    val dbAlerta = DbAlerta(
                        idSistema = 1, // Módulo de Residuos
                        idTipoAlerta = 1, // CRÍTICO
                        idEstadoAlerta = 1, // PENDIENTE
                        descripcion = "Contenedor $nombreLimpio al $porcentaje% — requiere vaciado",
                        fechaHora = java.time.OffsetDateTime.now().toString()
                    )
                    val result = AlertaRepository.insertarAlerta(dbAlerta)
                    if (result.isSuccess) {
                        android.util.Log.d("ResiduosMqttProcessor", "Alerta crítica de residuo insertada en Supabase")
                    }
                }
            }
        } else if (porcentaje < 20) {
            // Si el contenedor estaba marcado como crítico y ahora bajó del 20%, significa que fue vaciado con éxito
            if (alertasReportadas[claveAlerta] == true) {
                alertasReportadas[claveAlerta] = false

                // 1. Mostrar notificación push de éxito/vaciado
                NotificationHelper.enviarNotificacion(
                    context = context,
                    tipo = TipoAlerta.NORMAL,
                    titulo = "Contenedor $nombreLimpio Vaciado",
                    mensaje = "El contenedor de $nombreLimpio ha sido vaciado correctamente (nivel actual: $porcentaje%)."
                )

                // 2. Registrar el vaciado en Supabase
                scope.launch {
                    val dbAlerta = DbAlerta(
                        idSistema = 1, // Módulo de Residuos
                        idTipoAlerta = 4, // NORMAL
                        idEstadoAlerta = 2, // ATENDIDA (Se auto-resuelve)
                        descripcion = "Contenedor de $nombreLimpio vaciado con éxito (nivel actual: $porcentaje%)",
                        fechaHora = java.time.OffsetDateTime.now().toString()
                    )
                    val result = AlertaRepository.insertarAlerta(dbAlerta)
                    if (result.isSuccess) {
                        android.util.Log.d("MqttManager", "Registro de contenedor vaciado insertado en Supabase")
                    }
                }
            }
        } else {
            // Histeresis: si bajó del umbral de alerta (ej: a 70%) pero no hasta llegar a ser vaciado del todo,
            // limpiamos la bandera para permitir futuras alertas críticas si vuelve a subir.
            if (porcentaje < 75) {
                alertasReportadas[claveAlerta] = false
            }
        }
    }
}
