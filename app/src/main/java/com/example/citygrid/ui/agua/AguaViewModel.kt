package com.example.citygrid.ui.agua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.MqttManager
import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.model.AguaState
import com.example.citygrid.model.db.DbLecturaAgua
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AguaViewModel : ViewModel() {
    private val _aguaState = MutableStateFlow(AguaState())
    val aguaState: StateFlow<AguaState> = _aguaState.asStateFlow()

    private val _historial = MutableStateFlow<List<DbLecturaAgua>>(emptyList())
    val historial: StateFlow<List<DbLecturaAgua>> = _historial.asStateFlow()

    init {
        viewModelScope.launch {
            MqttManager.aguaFlow.collect { estadoMqtt ->
                _aguaState.value = estadoMqtt
            }
        }
        cargarHistorial()
    }

    fun cargarHistorial(idTanque: Int = 1) {
        viewModelScope.launch {
            try {
                val lecturas = SupabaseManager.client
                    .from("lecturasagua")
                    .select { filter { eq("idtanque", idTanque) } }
                    .decodeList<DbLecturaAgua>()

                val listaOrdenada = lecturas.sortedByDescending { it.fechaHora }
                _historial.value = listaOrdenada

                if (listaOrdenada.isNotEmpty()) {
                    val ultimaLectura = listaOrdenada.first()
                    val nivelReal = ultimaLectura.nivelAgua.toInt()

                    _aguaState.value = AguaState(
                        nivelTanque = nivelReal,
                        conectado = true,
                        bombaActiva = nivelReal < 20,
                        estadoGeneral = if (nivelReal >= 20) "Operando correctamente" else "Nivel Crítico",
                        ultimaActualizacion = System.currentTimeMillis()
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}