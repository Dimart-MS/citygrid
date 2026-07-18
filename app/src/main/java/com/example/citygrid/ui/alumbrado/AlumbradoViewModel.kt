package com.example.citygrid.ui.alumbrado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.repository.AlumbradoRepository
import com.example.citygrid.model.AlumbradoState
import com.example.citygrid.model.db.DbLecturaLuminaria
import com.example.citygrid.utils.Constants
import com.example.citygrid.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlumbradoViewModel : ViewModel() {
    // Usar directamente el flujo de MqttManager
    val alumbradoState: StateFlow<AlumbradoState> = MqttManager.alumbradoFlow

    private val _historial = MutableStateFlow<List<DbLecturaLuminaria>>(emptyList())
    val historial: StateFlow<List<DbLecturaLuminaria>> = _historial.asStateFlow()

    /** Estado de envío de comando MQTT para feedback visual */
    private val _enviandoComando = MutableStateFlow(false)
    val enviandoComando: StateFlow<Boolean> = _enviandoComando.asStateFlow()

    init {
        cargarHistorial()
    }

    fun cargarHistorial(idLuminaria: Int = 1) {
        viewModelScope.launch {
            try {
                val lecturas = AlumbradoRepository.obtenerUltimasLecturasLuminaria(idLuminaria, Constants.MAX_HISTORIAL_LECTURAS)
                _historial.value = lecturas
            } catch (e: Exception) {
                Logger.e("AlumbradoViewModel", "Error al cargar historial", e)
            }
        }
    }

    /**
     * Alterna las luces manualmente enviando comando MQTT.
     * Incluye feedback visual con estado de envío y actualización optimista.
     */
    fun alternarLucesManual(encender: Boolean) {
        viewModelScope.launch {
            _enviandoComando.value = true

            try {
                // Enviar instrucción al ESP32 por MQTT usando la constante correcta
                val payload = if (encender) "1" else "0"
                MqttManager.publish(Constants.TOPIC_CTRL_LUCES, payload)

                // Actualización optimista inmediata para evitar snapback en la UI
                val currentState = MqttManager.alumbradoFlow.value
                MqttManager.updateAlumbradoState(
                    currentState.copy(
                        estadoOn = encender,
                        modo = "MANUAL",
                        ultimaActualizacion = System.currentTimeMillis()
                    )
                )

                kotlinx.coroutines.delay(500)
            } finally {
                _enviandoComando.value = false
            }
        }
    }

    /**
     * Cambia entre modo AUTO y MANUAL.
     * En AUTO, el ESP32 usa el sensor LDR para controlar las luces automáticamente.
     */
    fun cambiarModo(automatico: Boolean) {
        viewModelScope.launch {
            _enviandoComando.value = true

            try {
                val payload = if (automatico) "2" else {
                    if (MqttManager.alumbradoFlow.value.estadoOn) "1" else "0"
                }
                MqttManager.publish(Constants.TOPIC_CTRL_LUCES, payload)

                // Actualización optimista inmediata
                val currentState = MqttManager.alumbradoFlow.value
                MqttManager.updateAlumbradoState(
                    currentState.copy(
                        modo = if (automatico) "AUTO" else "MANUAL",
                        ultimaActualizacion = System.currentTimeMillis()
                    )
                )

                kotlinx.coroutines.delay(500)
            } finally {
                _enviandoComando.value = false
            }
        }
    }
}
