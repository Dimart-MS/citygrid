package com.example.citygrid.ui.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.SessionManager
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.data.repository.BitacoraRepository
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta
import com.example.citygrid.model.db.toAlerta
import com.example.citygrid.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado combinado de alertas — evita 4 combines independientes. */
data class AlertasUiState(
    val alertas: List<Alerta> = emptyList(),
    val criticalCount: Int = 0,
    val warningCount: Int = 0,
    val systemCounts: Map<String, Int> = emptyMap()
)

class AlertasViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("Todas")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _dbAlertas = MutableStateFlow<List<DbAlerta>>(emptyList())

    /** Estado unificado — un solo combine sobre los mismos flows. */
    val uiState: StateFlow<AlertasUiState> = combine(
        _dbAlertas,
        MqttManager.alertasFlow,
        _selectedFilter
    ) { dbAlertas, mqttAlertas, filter ->
        val dbMapped = dbAlertas.map { it.toAlerta() }
        val todas = dbMapped + mqttAlertas

        // Filtrado
        val filtered = if (filter == "Todas") {
            todas.sortedByDescending { it.timestamp }
        } else {
            todas.filter { it.sistema.equals(filter, ignoreCase = true) }
                .sortedByDescending { it.timestamp }
        }

        // Conteos
        val dbCriticas = dbAlertas.count { it.idTipoAlerta == 1 && it.idEstadoAlerta != 2 }
        val dbAdvertencias = dbAlertas.count { it.idTipoAlerta == 2 && it.idEstadoAlerta != 2 }
        val mqttCriticas = mqttAlertas.count { it.tipo == TipoAlerta.CRITICO && !it.atendida }
        val mqttAdvertencias = mqttAlertas.count { it.tipo == TipoAlerta.ADVERTENCIA && !it.atendida }

        // Conteo por sistema
        val dbBySystem = dbAlertas.filter { it.idEstadoAlerta != 2 }
            .groupingBy { when (it.idSistema) { 1 -> "Residuos"; 2 -> "Agua"; 3 -> "Alumbrado"; else -> "Sistema" } }
            .eachCount()
        val mqttBySystem = mqttAlertas.filter { !it.atendida }
            .groupingBy { it.sistema.ifBlank { "Sistema" } }
            .eachCount()
        val sysCounts = mutableMapOf<String, Int>().apply {
            (dbBySystem.keys + mqttBySystem.keys + listOf("Residuos", "Agua", "Alumbrado", "Sistema")).forEach { key ->
                put(key, (dbBySystem[key] ?: 0) + (mqttBySystem[key] ?: 0))
            }
        }

        AlertasUiState(
            alertas = filtered,
            criticalCount = dbCriticas + mqttCriticas,
            warningCount = dbAdvertencias + mqttAdvertencias,
            systemCounts = sysCounts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlertasUiState())

    // Propiedades derivadas para compatibilidad con UI existente
    val filteredAlertas: StateFlow<List<Alerta>> = uiState
        .map { it.alertas }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val criticalCount: StateFlow<Int> = uiState
        .map { it.criticalCount }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val warningCount: StateFlow<Int> = uiState
        .map { it.warningCount }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val systemCounts: StateFlow<Map<String, Int>> = uiState
        .map { it.systemCounts }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        cargarAlertas()
    }

    private fun cargarAlertas() {
        viewModelScope.launch {
            val iniciales = AlertaRepository.obtenerAlertas()
            Logger.d("AlertasViewModel", "Cargadas ${iniciales.size} alertas iniciales desde Supabase REST API.")
            _dbAlertas.value = iniciales

            launch {
                try {
                    AlertaRepository.escucharAlertasRealtime().collect { actualizadas ->
                        if (actualizadas.isNotEmpty()) _dbAlertas.value = actualizadas
                    }
                } catch (e: Exception) {
                    Logger.e("AlertasViewModel", "Error al escuchar Realtime de Supabase", e)
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun marcarComoAtendida(idAlerta: String) {
        val idLong = idAlerta.toLongOrNull() ?: return
        viewModelScope.launch {
            val result = AlertaRepository.marcarAlertaComoAtendida(idLong)
            if (result.isSuccess) {
                Logger.d("AlertasViewModel", "Alerta $idLong marcada como atendida exitosamente.")
                BitacoraRepository.registrar(
                    idUsuario = sessionManager.getIdUsuario(),
                    accion = "ALERTA_ATENDIDA",
                    descripcion = "Alerta id=$idLong marcada como atendida"
                )
                _dbAlertas.value = AlertaRepository.obtenerAlertas()
            } else {
                Logger.e("AlertasViewModel", "Error al marcar alerta como atendida", result.exceptionOrNull())
            }
        }
    }
}
