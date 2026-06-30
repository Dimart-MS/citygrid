package com.example.citygrid.data.processor

import android.content.Context
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.AlumbradoPayload
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.data.repository.AlumbradoRepository
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta
import com.example.citygrid.utils.Constants
import com.example.citygrid.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

object AlumbradoMqttProcessor {
    private val alertasReportadas = mutableMapOf<String, Boolean>()

    fun procesarMensaje(topic: String, payload: String, context: Context, scope: CoroutineScope) {
        scope.launch {
            try {
                val data = Json.decodeFromString<AlumbradoPayload>(payload)
                val valorLdr = data.ldrLux ?: (if (data.estadoOn == true) 45 else 800)
                val encendido = data.estadoOn ?: (valorLdr < Constants.UMBRAL_LUZ_ADC)

                // Actualizar el StateFlow INMEDIATAMENTE para refrescar la UI sin esperar Supabase Realtime
                MqttManager.updateAlumbradoState(
                    MqttManager.alumbradoFlow.value.copy(
                        estadoOn = encendido,
                        condicionNoche = data.condicionNoche ?: encendido,
                        ldrLux = valorLdr,
                        luminariasActivas = data.luminariasActivas ?: (if (encendido) 12 else 0),
                        modo = data.modo ?: "AUTO",
                        ultimaActualizacion = System.currentTimeMillis()
                    )
                )

                // Verificar alertas de alumbrado de manera directa
                verificarAlertasAlumbrado(encendido, context, this)

                // Guardar en Supabase en background para el historial
                AlumbradoRepository.insertarLecturaLuminaria(idLuminaria = 1, valorLdr = valorLdr)
            } catch (e: Exception) {
                android.util.Log.e("AlumbradoMqttProcessor", "Error al procesar mensaje de Alumbrado", e)
            }
        }
    }

    fun verificarAlertasAlumbrado(encendido: Boolean, context: Context, scope: CoroutineScope) {
        if (encendido) {
            val claveAlerta = "luces_encendidas"
            if (alertasReportadas[claveAlerta] != true) {
                alertasReportadas[claveAlerta] = true

                // 1. Mostrar notificación push de inmediato
                NotificationHelper.enviarNotificacion(
                    context = context,
                    tipo = TipoAlerta.INFORMACION,
                    titulo = "Alumbrado Público Activo",
                    mensaje = "Las luces exteriores se han encendido automáticamente por detección de oscuridad."
                )

                // 2. Registrar la alerta en la Base de Datos (Supabase) en segundo plano
                scope.launch {
                    val dbAlerta = DbAlerta(
                        idSistema = 3, // Módulo de Alumbrado
                        idTipoAlerta = 3, // INFORMACIÓN
                        idEstadoAlerta = 1, // PENDIENTE
                        descripcion = "Alumbrado público encendido automáticamente por sensor LDR",
                        fechaHora = java.time.OffsetDateTime.now().toString()
                    )
                    val result = AlertaRepository.insertarAlerta(dbAlerta)
                    if (result.isSuccess) {
                        android.util.Log.d("AlumbradoMqttProcessor", "Alerta de alumbrado nocturno insertada en Supabase")
                    }
                }
            }
        } else {
            alertasReportadas["luces_encendidas"] = false
        }
    }
}
