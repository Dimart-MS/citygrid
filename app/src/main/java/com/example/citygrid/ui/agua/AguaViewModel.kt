package com.example.citygrid.ui.agua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.repository.AguaRepository
import com.example.citygrid.model.AguaState
import com.example.citygrid.model.db.DbLecturaAgua
import com.example.citygrid.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AguaViewModel : ViewModel() {
    // Usar directamente el flujo de MqttManager
    val aguaState: StateFlow<AguaState> = MqttManager.aguaFlow

    private val _historial = MutableStateFlow<List<DbLecturaAgua>>(emptyList())
    val historial: StateFlow<List<DbLecturaAgua>> = _historial.asStateFlow()

    init {
        cargarHistorial()
    }

    fun cargarHistorial(idTanque: Int = 1) {
        viewModelScope.launch {
            try {
                val lecturas = AguaRepository.obtenerLecturasPorTanque(idTanque)
                val listaOrdenada = lecturas.sortedByDescending { it.fechaHora }
                _historial.value = listaOrdenada
            } catch (e: Exception) {
                android.util.Log.e("AguaViewModel", "Error al cargar historial", e)
            }
        }
    }

    /**
     * Envía un comando al ESP32 para activar o desactivar la bomba manualmente.
     * El ESP32 vuelve al modo automático después de 60 segundos (TIEMPO_OVERRIDE_MANUAL_MS).
     */
    fun activarBombaManual(encender: Boolean) {
        val payload = if (encender) "1" else "0"
        MqttManager.publish(Constants.TOPIC_CTRL_BOMBA, payload)
    }
}