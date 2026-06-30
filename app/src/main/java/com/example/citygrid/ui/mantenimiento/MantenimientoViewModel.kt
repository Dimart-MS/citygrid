package com.example.citygrid.ui.mantenimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citygrid.data.SessionManager
import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.data.repository.BitacoraRepository
import com.example.citygrid.model.db.DbComponente
import com.example.citygrid.model.db.DbMantenimiento
import com.example.citygrid.model.db.DbMantenimientoInsert
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Data class ligera solo para cargar nombre de usuario en el ViewModel (sin campos sensibles). */
@Serializable
private data class UsuarioNombre(
    @SerialName("idusuario") val idUsuario: Int,
    @SerialName("nombre") val nombre: String
)

class MantenimientoViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _mantenimientos = MutableStateFlow<List<DbMantenimiento>>(emptyList())
    val mantenimientos: StateFlow<List<DbMantenimiento>> = _mantenimientos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _usuariosMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val usuariosMap: StateFlow<Map<Int, String>> = _usuariosMap.asStateFlow()

    private val _componentes = MutableStateFlow<List<DbComponente>>(emptyList())
    val componentes: StateFlow<List<DbComponente>> = _componentes.asStateFlow()

    init {
        cargarUsuarios()
        cargarComponentes()
        cargarMantenimientos()
    }

    private fun cargarUsuarios() {
        viewModelScope.launch {
            try {
                val listaUsuarios = SupabaseManager.client
                    .from("usuarios")
                    .select()
                    .decodeList<UsuarioNombre>()
                _usuariosMap.value = listaUsuarios.associate { it.idUsuario to it.nombre }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Carga la lista de componentes desde Supabase para el DropdownMenu de la pantalla. */
    private fun cargarComponentes() {
        viewModelScope.launch {
            try {
                val lista = SupabaseManager.client
                    .from("componentes")
                    .select()
                    .decodeList<DbComponente>()
                _componentes.value = lista
            } catch (e: Exception) {
                android.util.Log.e("MantenimientoViewModel", "Error al cargar componentes", e)
            }
        }
    }

    fun cargarMantenimientos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val lista = SupabaseManager.client
                    .from("mantenimientos")
                    .select()
                    .decodeList<DbMantenimiento>()

                _mantenimientos.value = lista.sortedByDescending { it.fechaMantenimiento }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registrarMantenimiento(idComponente: Int, tipo: String, descripcion: String, fechaTexto: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fechaIso = try {
                    val formatterInput = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val localDate = LocalDate.parse(fechaTexto, formatterInput)
                    localDate.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                } catch (e: Exception) {
                    OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                }

                val nuevoRegistro = DbMantenimientoInsert(
                    idComponente = idComponente,
                    idUsuario = sessionManager.getIdUsuario(),
                    descripcion = descripcion,
                    tipo = tipo,
                    fechaMantenimiento = fechaIso
                )

                SupabaseManager.client
                    .from("mantenimientos")
                    .insert(nuevoRegistro)

                // Registrar acción en la bitácora del sistema
                BitacoraRepository.registrar(
                    idUsuario = sessionManager.getIdUsuario() ?: 0,
                    accion = "MANTENIMIENTO_REGISTRO",
                    descripcion = "Tipo=$tipo, componente=$idComponente"
                )

                cargarMantenimientos()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}