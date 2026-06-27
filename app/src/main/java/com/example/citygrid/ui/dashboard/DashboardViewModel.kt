package com.example.citygrid.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.repository.AguaRepository
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.data.repository.AlumbradoRepository
import com.example.citygrid.data.repository.ResiduosRepository
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.TipoAlerta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

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

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _cargando.value = true
            launch { cargarResiduos() }
            launch { cargarAgua() }
            launch { cargarAlumbrado() }
            launch { cargarAlertas() }
            _cargando.value = false
        }
    }

    private suspend fun cargarResiduos() {
        val contenedores = ResiduosRepository.obtenerContenedores()
        if (contenedores.isEmpty()) return

        var maxLlenado = -1
        var nombreMax = ""
        for (c in contenedores) {
            val lecturas = ResiduosRepository.obtenerLecturasPorContenedor(c.idContenedor ?: continue)
            if (lecturas.isEmpty()) continue
            val ultima = lecturas.maxByOrNull { it.fechaHora ?: "" }
            val nivel = ultima?.nivelLlenado?.toInt() ?: 0
            if (nivel > maxLlenado) {
                maxLlenado = nivel
                nombreMax = c.nombre
            }
        }
        _residuoPorcentaje.value = maxLlenado.coerceIn(0, 100)
        _residuoNombre.value = nombreMax
    }

    private suspend fun cargarAgua() {
        val tanques = AguaRepository.obtenerTanques()
        if (tanques.isEmpty()) return
        val tanque = tanques.first()
        val id = tanque.idTanque ?: return
        val lecturas = AguaRepository.obtenerLecturasPorTanque(id)
        if (lecturas.isNotEmpty()) {
            val ultima = lecturas.maxByOrNull { it.fechaHora ?: "" }
            _nivelAgua.value = (ultima?.nivelAgua?.toInt() ?: 0).coerceIn(0, 100)
        }
        val bombas = AguaRepository.obtenerBombas()
        _bombaActiva.value = bombas.any { it.estado }
    }

    private suspend fun cargarAlumbrado() {
        val luminarias = AlumbradoRepository.obtenerLuminarias()
        if (luminarias.isEmpty()) return
        val luminaria = luminarias.first()
        val id = luminaria.idLuminaria ?: return
        val lecturas = AlumbradoRepository.obtenerLecturasPorLuminaria(id)
        if (lecturas.isNotEmpty()) {
            val ultima = lecturas.maxByOrNull { it.fechaHora ?: "" }
            _ldrLux.value = ultima?.valorLdr ?: 0
        }
        _alumbradoOn.value = luminaria.estado
    }

    private suspend fun cargarAlertas() {
        val dbAlertas = AlertaRepository.obtenerAlertas()
        val uiAlertas = dbAlertas.map { db ->
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
        _alertas.value = uiAlertas
    }

    val alertasActivas: Int get() = _alertas.value.count { !it.atendida }
    val alertasCriticas: Int get() = _alertas.value.count { it.tipo == TipoAlerta.CRITICO && !it.atendida }
    val alertasAdvertencia: Int get() = _alertas.value.count { it.tipo == TipoAlerta.ADVERTENCIA && !it.atendida }
}
