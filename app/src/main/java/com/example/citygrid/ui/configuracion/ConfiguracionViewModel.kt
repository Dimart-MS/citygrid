package com.example.citygrid.ui.configuracion

import androidx.lifecycle.ViewModel
import com.example.citygrid.data.SessionManager

class ConfiguracionViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    fun isDarkMode(): Boolean = sessionManager.isTemaOscuro()

    fun setDarkMode(activo: Boolean) {
        sessionManager.setTemaOscuro(activo)
    }

    fun getNombre(): String = sessionManager.getNombre()

    fun getCorreo(): String = sessionManager.getCorreo()

    fun cerrarSesion() {
        sessionManager.cerrarSesion()
    }
}
