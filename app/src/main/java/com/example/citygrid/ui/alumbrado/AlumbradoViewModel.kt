package com.example.citygrid.ui.alumbrado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.repository.AlumbradoRepository
import com.example.citygrid.model.AlumbradoState
import com.example.citygrid.model.db.DbLecturaLuminaria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlumbradoViewModel : ViewModel() {

    private val _alumbradoState = MutableStateFlow(AlumbradoState())
    val alumbradoState: StateFlow<AlumbradoState> = _alumbradoState.asStateFlow()

    private val _historial = MutableStateFlow<List<DbLecturaLuminaria>>(emptyList())
    val historial: StateFlow<List<DbLecturaLuminaria>> = _historial.asStateFlow()

    init {
        viewModelScope.launch {
            MqttManager.alumbradoFlow.collect { estadoMqtt ->
                _alumbradoState.value = estadoMqtt
            }
        }
        cargarHistorial()
    }

    fun cargarHistorial(idLuminaria: Int = 1) {
        viewModelScope.launch {
            val lecturas = AlumbradoRepository.obtenerLecturasPorLuminaria(idLuminaria)
            _historial.value = lecturas.sortedByDescending { it.fechaHora }
        }
    }

    fun alternarLucesManual(encender: Boolean) {
        // Enviar instrucción al ESP32 por MQTT
        val payload = if (encender) "1" else "0"
        MqttManager.publish("control-luces", payload)

        _alumbradoState.value = _alumbradoState.value.copy(
            estadoOn = encender,
            modo = "MANUAL" // Cambiamos el texto de la tarjeta a MANUAL
        )
    }
}