package com.example.citygrid.ui.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.data.repository.BitacoraRepository
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertasViewModel : ViewModel() {

    private val _selectedFilter = MutableStateFlow("Todas")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _dbAlertas = MutableStateFlow<List<DbAlerta>>(emptyList())

    // Mapeo reactivo de DbAlerta a Alerta para renderizado de interfaz y filtrado en ViewModel
    // Combina alertas de DB + MQTT
    val filteredAlertas: StateFlow<List<Alerta>> = combine(
        _dbAlertas,
        MqttManager.alertasFlow,
        _selectedFilter
    ) { dbAlertas, mqttAlertas, filter ->
        // Convertir alertas de DB
        val dbMapped = dbAlertas.map { dbAlerta ->
            val sistemaName = when (dbAlerta.idSistema) {
                1 -> "Residuos"
                2 -> "Agua"
                3 -> "Alumbrado"
                else -> "Sistema"
            }
            val tipoAlertaVal = when (dbAlerta.idTipoAlerta) {
                1 -> TipoAlerta.CRITICO
                2 -> TipoAlerta.ADVERTENCIA
                3 -> TipoAlerta.INFORMACION
                4 -> TipoAlerta.NORMAL
                else -> TipoAlerta.INFORMACION
            }
            val atendidaVal = dbAlerta.idEstadoAlerta == 2

            val parsedTime = try {
                dbAlerta.fechaHora?.let {
                    java.time.OffsetDateTime.parse(it).toInstant().toEpochMilli()
                } ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            Alerta(
                id = dbAlerta.idAlerta?.toString() ?: "",
                tipo = tipoAlertaVal,
                titulo = "${sistemaName} - Incidencia",
                descripcion = dbAlerta.descripcion,
                sistema = sistemaName,
                timestamp = parsedTime,
                atendida = atendidaVal
            )
        }

        // Combinar DB + MQTT
        val todasAlertas = dbMapped + mqttAlertas

        if (filter == "Todas") {
            todasAlertas.sortedByDescending { it.timestamp }
        } else {
            todasAlertas.filter { it.sistema.equals(filter, ignoreCase = true) }
                .sortedByDescending { it.timestamp }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Contadores de alertas pendientes calculados dinámicamente desde DB + MQTT
    val criticalCount: StateFlow<Int> = combine(
        _dbAlertas,
        MqttManager.alertasFlow
    ) { dbAlertas, mqttAlertas ->
        val dbCriticas = dbAlertas.count { it.idTipoAlerta == 1 && it.idEstadoAlerta != 2 }
        val mqttCriticas = mqttAlertas.count { it.tipo == TipoAlerta.CRITICO && !it.atendida }
        dbCriticas + mqttCriticas
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val warningCount: StateFlow<Int> = combine(
        _dbAlertas,
        MqttManager.alertasFlow
    ) { dbAlertas, mqttAlertas ->
        val dbAdvertencias = dbAlertas.count { it.idTipoAlerta == 2 && it.idEstadoAlerta != 2 }
        val mqttAdvertencias = mqttAlertas.count { it.tipo == TipoAlerta.ADVERTENCIA && !it.atendida }
        dbAdvertencias + mqttAdvertencias
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        cargarAlertas()
    }

    private fun cargarAlertas() {
        viewModelScope.launch {
            // 1. Cargar datos iniciales por REST (Postgrest)
            val iniciales = AlertaRepository.obtenerAlertas()
            android.util.Log.d("AlertasViewModel", "Cargadas ${iniciales.size} alertas iniciales desde Supabase REST API.")
            _dbAlertas.value = iniciales

            // 2. Conectar a Realtime y escuchar en segundo plano
            launch {
                try {
                    android.util.Log.d("AlertasViewModel", "Conectando a Supabase Realtime...")
                    SupabaseManager.client.realtime.connect()
                    AlertaRepository.escucharAlertasRealtime().collect { actualizadas ->
                        android.util.Log.d("AlertasViewModel", "Recibidas ${actualizadas.size} alertas actualizadas por Realtime.")
                        if (actualizadas.isNotEmpty()) {
                            _dbAlertas.value = actualizadas
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AlertasViewModel", "Error al conectar/escuchar Realtime de Supabase", e)
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
                android.util.Log.d("AlertasViewModel", "Alerta $idLong marcada como atendida exitosamente.")
                // Registrar en la bitácora del sistema
                BitacoraRepository.registrar(
                    idUsuario = 0, // TODO: pasar SessionManager al ViewModel si se requiere el ID real
                    accion = "ALERTA_ATENDIDA",
                    descripcion = "Alerta id=$idLong marcada como atendida"
                )
                // Volver a cargar para refrescar la lista local inmediatamente
                val actualizadas = AlertaRepository.obtenerAlertas()
                _dbAlertas.value = actualizadas
            } else {
                android.util.Log.e("AlertasViewModel", "Error al marcar alerta como atendida", result.exceptionOrNull())
            }
        }
    }
}
