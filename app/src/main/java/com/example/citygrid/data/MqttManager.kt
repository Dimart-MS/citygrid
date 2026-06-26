package com.example.citygrid.data

import com.example.citygrid.model.AguaState
import com.example.citygrid.model.AlumbradoState
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.ResiduosState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Fernando llena la implementación — esto es el contrato que todos consumen
object MqttManager {
    val residuosFlow: StateFlow<ResiduosState>   = MutableStateFlow(ResiduosState())
    val aguaFlow: StateFlow<AguaState>           = MutableStateFlow(AguaState())
    val alumbradoFlow: StateFlow<AlumbradoState> = MutableStateFlow(AlumbradoState())
    val alertasFlow: StateFlow<List<Alerta>>     = MutableStateFlow(emptyList())

    fun connect() { /* TODO: Fer implementa */ }
    fun disconnect() { /* TODO: Fer implementa */ }
    fun publish(topic: String, payload: String) { /* TODO: Fer implementa */ }
}