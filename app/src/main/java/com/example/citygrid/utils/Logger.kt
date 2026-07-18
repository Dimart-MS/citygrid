package com.example.citygrid.utils

import android.util.Log as AndroidLog

/**
 * Wrapper de logging que permite deshabilitar logs en producción.
 * Reemplaza todos los android.util.Log.* del proyecto.
 */
object Logger {

    /** Cambiar a false para silenciar logs en producción. */
    var isDebug = true

    fun d(tag: String, message: String) {
        if (isDebug) AndroidLog.d(tag, message)
    }

    fun w(tag: String, message: String) {
        if (isDebug) AndroidLog.w(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebug) AndroidLog.e(tag, message, throwable)
    }
}
