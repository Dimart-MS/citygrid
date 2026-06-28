package com.example.citygrid.data

import com.example.citygrid.model.AguaState
import com.example.citygrid.model.AlumbradoState
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.ResiduosState
import com.example.citygrid.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.eclipse.paho.client.mqttv3.MqttConnectOptions

// Fernando llena la implementación — esto es el contrato que todos consumen
object MqttManager {
    val residuosFlow: StateFlow<ResiduosState>   = MutableStateFlow(ResiduosState())
    val aguaFlow: StateFlow<AguaState>           = MutableStateFlow(AguaState())
    val alumbradoFlow: MutableStateFlow<AlumbradoState> = MutableStateFlow(
        AlumbradoState(
            estadoOn = true,
            condicionNoche = true,
            ldrLux = 45, // Pocos luxes = de noche
            luminariasActivas = 12,
            modo = "AUTO",
            ultimaActualizacion = System.currentTimeMillis(),
            conectado = true
        )
    )
    val alertasFlow: StateFlow<List<Alerta>>     = MutableStateFlow(emptyList())

    val mqttOptions = MqttConnectOptions().apply {
        userName = Constants.MQTT_USER
        password = Constants.MQTT_PASSWORD.toCharArray()
        isAutomaticReconnect = true
        isCleanSession = true
        connectionTimeout = 30
        keepAliveInterval = 60
    }

    fun connect() { /* TODO: Fer implementa */ }
    fun disconnect() { /* TODO: Fer implementa */ }
    fun publish(topic: String, payload: String) { /* TODO: Fer implementa */ }
}