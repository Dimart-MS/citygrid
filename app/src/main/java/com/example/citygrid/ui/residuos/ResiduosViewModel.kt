package com.example.citygrid.ui.residuos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.ContenedorData
import com.example.citygrid.model.ResiduosState
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta
import com.example.citygrid.model.db.toAlerta
import com.example.citygrid.utils.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ResiduosViewModel : ViewModel() {

    val residuosState: StateFlow<ResiduosState> = MqttManager.residuosFlow

    // Contenedor más lleno calculado reactivamente (priorizando activos)
    val maxContenedor: StateFlow<ContenedorData?> = residuosState.map { state ->
        state.contenedores.filter { it.activo }.maxByOrNull { it.porcentaje }
            ?: state.contenedores.maxByOrNull { it.porcentaje }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Cantidad de contenedores activos en estado crítico (>85%)
    val criticalContainersCount: StateFlow<Int> = residuosState.map { state ->
        state.contenedores.count { it.activo && it.porcentaje > 85 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Alertas y eventos recientes para el módulo de residuos (Combina DB + MQTT)
    private val _dbAlertas = MutableStateFlow<List<DbAlerta>>(emptyList())

    val eventosRecientes: StateFlow<List<Alerta>> = combine(
        _dbAlertas,
        MqttManager.alertasFlow
    ) { dbAlertas, mqttAlertas ->
        // Convertir y filtrar alertas de base de datos para el sistema de Residuos (idSistema = 1)
        val dbMapped = dbAlertas.filter { it.idSistema == 1 }.map { it.toAlerta() }

        // Filtrar alertas MQTT para Residuos
        val mqttFiltered = mqttAlertas.filter { it.sistema.equals("Residuos", ignoreCase = true) }

        // Combinar ambas listas, ordenar por timestamp descendente y tomar las últimas 3 (según mockup)
        (dbMapped + mqttFiltered).sortedByDescending { it.timestamp }.take(3)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        cargarAlertas()
    }

    fun cargarAlertas() {
        viewModelScope.launch {
            try {
                val alertas = AlertaRepository.obtenerAlertas()
                _dbAlertas.value = alertas
            } catch (e: Exception) {
                Logger.e("ResiduosViewModel", "Error al cargar alertas de base de datos", e)
            }
        }
    }
}
