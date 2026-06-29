package com.example.citygrid.data.processor

import android.content.Context
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.AlertaPayload
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.utils.NotificationHelper
import kotlinx.serialization.json.Json
import java.util.UUID

object AlertasMqttProcessor {

    fun procesarAlerta(payload: String, context: Context) {
        try {
            // Intenta procesar como JSON estructurado
            val data = Json.decodeFromString<AlertaPayload>(payload)
            val tipoStr = data.tipo ?: "INFORMACION"
            val tipoAlerta = try { TipoAlerta.valueOf(tipoStr.uppercase()) } catch (e: Exception) { TipoAlerta.INFORMACION }
            val titulo = data.titulo ?: "Nueva Alerta de CityGrid"
            val desc = data.descripcion ?: "Se ha recibido un aviso del sistema."
            
            NotificationHelper.enviarNotificacion(context, tipoAlerta, titulo, desc)
            
            // Agregar al listado local de alertas MQTT
            val nuevaAlerta = Alerta(
                id = UUID.randomUUID().toString(),
                tipo = tipoAlerta,
                titulo = titulo,
                descripcion = desc,
                sistema = when (data.idSistema) {
                    1 -> "Residuos"
                    2 -> "Agua"
                    3 -> "Alumbrado"
                    else -> "Sistema"
                },
                timestamp = System.currentTimeMillis(),
                atendida = false
            )
            MqttManager.addMqttAlerta(nuevaAlerta)
        } catch (e: Exception) {
            // Si no es un JSON, procesar como texto plano (tipo advertencia)
            android.util.Log.d("AlertasMqttProcessor", "Procesando payload de alerta como texto plano: $payload")
            NotificationHelper.enviarNotificacion(context, TipoAlerta.ADVERTENCIA, "Alerta del Sistema", payload)
            
            val nuevaAlerta = Alerta(
                id = UUID.randomUUID().toString(),
                tipo = TipoAlerta.ADVERTENCIA,
                titulo = "Alerta de Dispositivo",
                descripcion = payload,
                sistema = "Sistema",
                timestamp = System.currentTimeMillis(),
                atendida = false
            )
            MqttManager.addMqttAlerta(nuevaAlerta)
        }
    }
}
