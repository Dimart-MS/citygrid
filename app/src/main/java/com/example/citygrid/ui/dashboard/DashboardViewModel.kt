package com.example.citygrid.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta
import com.example.citygrid.model.db.toAlerta
import com.example.citygrid.utils.Constants
import com.example.citygrid.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel del Dashboard que combina datos de múltiples subsistemas.
 *
 * En lugar de replicar los StateFlows de MqttManager, expone directamente
 * los flows originales y solo mantiene estado adicional para alertas combinadas.
 */
class DashboardViewModel : ViewModel() {

    // Exponer directamente los flows de MqttManager sin duplicación
    val residuosState = MqttManager.residuosFlow
    val aguaState = MqttManager.aguaFlow
    val alumbradoState = MqttManager.alumbradoFlow

    /** Estado de conexión del ESP32 — true si llegó al menos un mensaje MQTT en los últimos 2 minutos. */
    val esp32Conectado: StateFlow<Boolean> = residuosState
        .map { it.conectado }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _dbAlertas = MutableStateFlow<List<DbAlerta>>(emptyList())

    private val _alertas = MutableStateFlow<List<Alerta>>(emptyList())
    val alertas: StateFlow<List<Alerta>> = _alertas.asStateFlow()

    init {
        // 1. Cargar alertas iniciales de la DB por REST y luego escuchar Realtime
        viewModelScope.launch {
            try {
                val iniciales = AlertaRepository.obtenerAlertas()
                _dbAlertas.value = iniciales

                launch {
                    try {
                        AlertaRepository.escucharAlertasRealtime().collect { actualizadas ->
                            if (actualizadas.isNotEmpty()) _dbAlertas.value = actualizadas
                        }
                    } catch (e: Exception) {
                        Logger.e("DashboardViewModel", "Error al escuchar Realtime en Dashboard", e)
                    }
                }
            } catch (e: Exception) {
                Logger.e("DashboardViewModel", "Error al cargar alertas iniciales", e)
            }
        }

        // 2. Combinar alertas de DB (mapeadas reactivamente) y MQTT
        viewModelScope.launch {
            combine(
                _dbAlertas.map { dbList -> dbList.map { it.toAlerta() } },
                MqttManager.alertasFlow
            ) { dbAlertas, mqttAlertas ->
                (dbAlertas + mqttAlertas).sortedByDescending { it.timestamp }.take(Constants.MAX_ALERTAS_DASHBOARD)
            }.collect { combinedAlertas ->
                _alertas.value = combinedAlertas
            }
        }
    }

    val alertasActivas: Int get() = _alertas.value.count { !it.atendida }
    val alertasCriticas: Int get() = _alertas.value.count { it.tipo == TipoAlerta.CRITICO && !it.atendida }
    val alertasAdvertencia: Int get() = _alertas.value.count { it.tipo == TipoAlerta.ADVERTENCIA && !it.atendida }
}
