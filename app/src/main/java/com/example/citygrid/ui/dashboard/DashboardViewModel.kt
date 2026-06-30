package com.example.citygrid.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.ContenedorData
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

class DashboardViewModel : ViewModel() {

    // Datos de MQTT en tiempo real
    private val _residuoNombre = MutableStateFlow("--")
    val residuoNombre: StateFlow<String> = _residuoNombre.asStateFlow()

    private val _residuoPorcentaje = MutableStateFlow(0)
    val residuoPorcentaje: StateFlow<Int> = _residuoPorcentaje.asStateFlow()

    private val _nivelAgua = MutableStateFlow(0)
    val nivelAgua: StateFlow<Int> = _nivelAgua.asStateFlow()

    private val _bombaActiva = MutableStateFlow(false)
    val bombaActiva: StateFlow<Boolean> = _bombaActiva.asStateFlow()

    private val _alumbradoOn = MutableStateFlow(false)
    val alumbradoOn: StateFlow<Boolean> = _alumbradoOn.asStateFlow()

    private val _ldrLux = MutableStateFlow(0)
    val ldrLux: StateFlow<Int> = _ldrLux.asStateFlow()

    private val _alertas = MutableStateFlow<List<Alerta>>(emptyList())
    val alertas: StateFlow<List<Alerta>> = _alertas.asStateFlow()

    private val _dbAlertas = MutableStateFlow<List<DbAlerta>>(emptyList())

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _ultimaActualizacion = MutableStateFlow(0L)
    val ultimaActualizacion: StateFlow<Long> = _ultimaActualizacion.asStateFlow()

    /** Estado de conexión del ESP32 — true si llegó al menos un mensaje MQTT en los últimos 2 minutos. */
    val esp32Conectado: StateFlow<Boolean> = MqttManager.residuosFlow
        .map { it.conectado }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        // Escuchar flujos MQTT en tiempo real
        viewModelScope.launch {
            MqttManager.residuosFlow.collect { state ->
                val maxContenedor = state.contenedores.maxByOrNull { it.porcentaje }
                _residuoNombre.value = maxContenedor?.nombre ?: "--"
                _residuoPorcentaje.value = maxContenedor?.porcentaje ?: 0
                _ultimaActualizacion.value = state.ultimoMensajeTimestamp
                _cargando.value = false
            }
        }

        viewModelScope.launch {
            MqttManager.aguaFlow.collect { state ->
                _nivelAgua.value = state.nivelTanque
                _bombaActiva.value = state.bombaActiva
                _ultimaActualizacion.value = state.ultimaActualizacion
            }
        }

        viewModelScope.launch {
            MqttManager.alumbradoFlow.collect { state ->
                _alumbradoOn.value = state.estadoOn
                _ldrLux.value = state.ldrLux
                _ultimaActualizacion.value = state.ultimaActualizacion
            }
        }

        // 1. Cargar alertas iniciales de la DB por REST y luego escuchar Realtime
        viewModelScope.launch {
            try {
                val iniciales = AlertaRepository.obtenerAlertas()
                _dbAlertas.value = iniciales
                
                launch {
                    try {
                        SupabaseManager.client.realtime.connect()
                        AlertaRepository.escucharAlertasRealtime().collect { actualizadas ->
                            if (actualizadas.isNotEmpty()) {
                                _dbAlertas.value = actualizadas
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardViewModel", "Error al conectar Realtime en Dashboard", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Error al cargar alertas iniciales", e)
            }
        }

        // 2. Combinar alertas de DB (mapeadas reactivamente) y MQTT
        viewModelScope.launch {
            combine(
                _dbAlertas.map { dbList ->
                    dbList.map { db ->
                        val sistema = when (db.idSistema) { 1 -> "Residuos"; 2 -> "Agua"; 3 -> "Alumbrado"; else -> "Sistema" }
                        val tipo = when (db.idTipoAlerta) { 1 -> TipoAlerta.CRITICO; 2 -> TipoAlerta.ADVERTENCIA; 3 -> TipoAlerta.INFORMACION; else -> TipoAlerta.NORMAL }
                        val parsedTime = try { db.fechaHora?.let { java.time.OffsetDateTime.parse(it).toInstant().toEpochMilli() } ?: System.currentTimeMillis() }
                        catch (_: Exception) { System.currentTimeMillis() }
                        Alerta(
                            id = db.idAlerta?.toString() ?: "",
                            tipo = tipo, titulo = "${sistema} - Incidencia",
                            descripcion = db.descripcion, sistema = sistema,
                            timestamp = parsedTime, atendida = db.idEstadoAlerta == 2
                        )
                    }
                },
                MqttManager.alertasFlow
            ) { dbAlertas, mqttAlertas ->
                (dbAlertas + mqttAlertas).sortedByDescending { it.timestamp }.take(10)
            }.collect { combinedAlertas ->
                _alertas.value = combinedAlertas
            }
        }
    }

    val alertasActivas: Int get() = _alertas.value.count { !it.atendida }
    val alertasCriticas: Int get() = _alertas.value.count { it.tipo == TipoAlerta.CRITICO && !it.atendida }
    val alertasAdvertencia: Int get() = _alertas.value.count { it.tipo == TipoAlerta.ADVERTENCIA && !it.atendida }
}
