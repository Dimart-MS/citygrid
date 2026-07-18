package com.example.citygrid.ui.agua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.repository.AguaRepository
import com.example.citygrid.model.AguaState
import com.example.citygrid.model.db.DbLecturaAgua
import com.example.citygrid.utils.Constants
import com.example.citygrid.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AguaViewModel : ViewModel() {
    // Usar directamente el flujo de MqttManager
    val aguaState: StateFlow<AguaState> = MqttManager.aguaFlow

    private val _historial = MutableStateFlow<List<DbLecturaAgua>>(emptyList())
    val historial: StateFlow<List<DbLecturaAgua>> = _historial.asStateFlow()

    /** Estado de envío de comando MQTT para feedback visual */
    private val _enviandoComando = MutableStateFlow(false)
    val enviandoComando: StateFlow<Boolean> = _enviandoComando.asStateFlow()

    init {
        cargarHistorial()
    }

    fun cargarHistorial(idTanque: Int = 1) {
        viewModelScope.launch {
            try {
                val lecturas = AguaRepository.obtenerUltimasLecturasAgua(idTanque, Constants.MAX_HISTORIAL_LECTURAS)
                _historial.value = lecturas
            } catch (e: Exception) {
                Logger.e("AguaViewModel", "Error al cargar historial", e)
            }
        }
    }

    /**
     * Envía un comando al ESP32 para activar o desactivar la bomba manualmente.
     * El ESP32 vuelve al modo automático después de 60 segundos (TIEMPO_OVERRIDE_MANUAL_MS).
     *
     * Incluye feedback visual con estado de envío y actualización optimista.
     */
    fun activarBombaManual(encender: Boolean) {
        viewModelScope.launch {
            // Activar indicador de envío
            _enviandoComando.value = true

            try {
                val payload = if (encender) "1" else "0"
                MqttManager.publish(Constants.TOPIC_CTRL_BOMBA, payload)

                // Actualización optimista inmediata para evitar snapback en la UI
                val currentState = MqttManager.aguaFlow.value
                MqttManager.updateAguaState(
                    currentState.copy(
                        bombaActiva = encender,
                        ultimaActualizacion = System.currentTimeMillis()
                    )
                )

                // Pequeño delay para simular tiempo de red
                kotlinx.coroutines.delay(500)
            } finally {
                // Desactivar indicador de envío
                _enviandoComando.value = false
            }
        }
    }
}
