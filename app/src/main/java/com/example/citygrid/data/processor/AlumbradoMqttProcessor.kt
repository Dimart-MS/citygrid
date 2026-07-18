package com.example.citygrid.data.processor

import android.content.Context
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.AlumbradoPayload
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.data.repository.AlumbradoRepository
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta
import com.example.citygrid.utils.AlertaHelper
import com.example.citygrid.utils.Constants
import com.example.citygrid.utils.Logger
import com.example.citygrid.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

object AlumbradoMqttProcessor {
    private val json = Json { ignoreUnknownKeys = true }

    fun procesarMensaje(topic: String, payload: String, context: Context, scope: CoroutineScope) {
        scope.launch {
            try {
                val data = json.decodeFromString<AlumbradoPayload>(payload)
                val valorLdr = data.ldrLux ?: (if (data.estadoOn == true) 45 else 800)
                val encendido = data.estadoOn ?: (valorLdr < Constants.UMBRAL_LUZ_ADC)

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

                verificarAlertasAlumbrado(encendido, context, this)

                AlumbradoRepository.insertarLecturaLuminaria(idLuminaria = 1, valorLdr = valorLdr)
            } catch (e: Exception) {
                Logger.e("AlumbradoMqttProcessor", "Error al procesar mensaje de Alumbrado", e)
            }
        }
    }

    fun verificarAlertasAlumbrado(encendido: Boolean, context: Context, scope: CoroutineScope) {
        val claveAlerta = "luces_encendidas"
        if (encendido) {
            scope.launch {
                AlertaHelper.emitirSiNueva(
                    clave = claveAlerta,
                    tipo = TipoAlerta.INFORMACION,
                    titulo = "Alumbrado Público Activo",
                    mensaje = "Las luces exteriores se han encendido automáticamente por detección de oscuridad.",
                    dbAlerta = DbAlerta(
                        idSistema = 3,
                        idTipoAlerta = 3,
                        idEstadoAlerta = 1,
                        descripcion = "Alumbrado público encendido automáticamente por sensor LDR",
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
