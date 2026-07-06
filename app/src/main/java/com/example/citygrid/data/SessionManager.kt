package com.example.citygrid.data

import android.content.Context
import android.content.SharedPreferences
import com.example.citygrid.utils.Constants

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun guardarSesion(correo: String, nombre: String, idUsuario: Int? = null) {
        prefs.edit()
            .putString(Constants.KEY_CORREO, correo)
            .putString(Constants.KEY_NOMBRE, nombre)
            .putBoolean(Constants.KEY_SESION_ACTIVA, true)
            .putLong(Constants.KEY_ULTIMA_SYNC, System.currentTimeMillis())
            .apply()
        idUsuario?.let { prefs.edit().putInt(Constants.KEY_ID_USUARIO, it).apply() }
    }

    fun cerrarSesion() {
        prefs.edit()
            .putBoolean(Constants.KEY_SESION_ACTIVA, false)
            .remove(Constants.KEY_CORREO)
            .remove(Constants.KEY_NOMBRE)
            .apply()
    }

    fun isSesionActiva(): Boolean =
        prefs.getBoolean(Constants.KEY_SESION_ACTIVA, false)

    fun getCorreo(): String =
        prefs.getString(Constants.KEY_CORREO, "") ?: ""

    fun getNombre(): String =
        prefs.getString(Constants.KEY_NOMBRE, "Administrador") ?: "Administrador"

    fun getUltimaSync(): Long =
        prefs.getLong(Constants.KEY_ULTIMA_SYNC, 0L)

    fun setTemaOscuro(activo: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_TEMA_OSCURO, activo).apply()
    }

    fun isTemaOscuro(): Boolean =
        prefs.getBoolean(Constants.KEY_TEMA_OSCURO, false)

    fun actualizarSync() {
        prefs.edit()
            .putLong(Constants.KEY_ULTIMA_SYNC, System.currentTimeMillis())
            .apply()
    }

    fun getIdUsuario(): Int = prefs.getInt(Constants.KEY_ID_USUARIO, 1)
}