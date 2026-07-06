package com.example.citygrid.ui.alumbrado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.repository.AlumbradoRepository
import com.example.citygrid.model.AlumbradoState
import com.example.citygrid.model.db.DbLecturaLuminaria
import com.example.citygrid.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlumbradoViewModel : ViewModel() {
    // Usar directamente el flujo de MqttManager
    val alumbradoState: StateFlow<AlumbradoState> = MqttManager.alumbradoFlow

    private val _historial = MutableStateFlow<List<DbLecturaLuminaria>>(emptyList())
    val historial: StateFlow<List<DbLecturaLuminaria>> = _historial.asStateFlow()

    init {
        cargarHistorial()
    }

    fun cargarHistorial(idLuminaria: Int = 1) {
        viewModelScope.launch {
            try {
                val lecturas = AlumbradoRepository.obtenerUltimasLecturasLuminaria(idLuminaria, 50)
                _historial.value = lecturas
            } catch (e: Exception) {
                android.util.Log.e("AlumbradoViewModel", "Error al cargar historial", e)
            }
        }
    }

    fun alternarLucesManual(encender: Boolean) {
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
    }

    fun cambiarModo(automatico: Boolean) {
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
    }
}
