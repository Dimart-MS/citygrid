package com.example.citygrid.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.citygrid.model.TipoAlerta

object NotificationHelper {

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Constants.CHANNEL_NAME
            val descriptionText = "Canal de alertas de CityGrid"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun enviarNotificacion(context: Context, tipo: TipoAlerta, titulo: String, mensaje: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Asignar icono, prioridad y color según la severidad (requisito del examen)
        val (smallIcon, priority, color) = when (tipo) {
            TipoAlerta.CRITICO -> Triple(
                android.R.drawable.stat_sys_warning,
                NotificationCompat.PRIORITY_MAX,
                0xFFC53030.toInt() // Rojo
            )
            TipoAlerta.ADVERTENCIA -> Triple(
                android.R.drawable.stat_sys_warning,
                NotificationCompat.PRIORITY_HIGH,
                0xFFD69E2E.toInt() // Amarillo/Dorado
            )
            TipoAlerta.INFORMACION -> Triple(
                android.R.drawable.ic_dialog_info,
                NotificationCompat.PRIORITY_DEFAULT,
                0xFF00A8CC.toInt() // Azul
            )
            TipoAlerta.NORMAL -> Triple(
                android.R.drawable.ic_dialog_info,
                NotificationCompat.PRIORITY_LOW,
                0xFF00A896.toInt() // Verde
            )
        }

        val builder = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(priority)
            .setColor(color)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 250, 250))

        val notificationId = System.currentTimeMillis().toInt()
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationHelper", "Falta permiso POST_NOTIFICATIONS", e)
        }
    }
}
