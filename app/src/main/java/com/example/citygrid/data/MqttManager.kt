package com.example.citygrid.data

import android.content.Context
import com.example.citygrid.model.AguaState
import com.example.citygrid.model.Alerta
import com.example.citygrid.model.AlumbradoState
import com.example.citygrid.model.ContenedorData
import com.example.citygrid.model.ResiduosState
import com.example.citygrid.model.TipoAlerta
import com.example.citygrid.model.db.DbAlerta
import com.example.citygrid.data.repository.AlertaRepository
import com.example.citygrid.data.repository.ResiduosRepository
import com.example.citygrid.data.processor.ResiduosMqttProcessor
import com.example.citygrid.data.processor.AguaMqttProcessor
import com.example.citygrid.data.processor.AlumbradoMqttProcessor
import com.example.citygrid.data.processor.AlertasMqttProcessor
import com.example.citygrid.utils.Constants
import com.example.citygrid.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.*
import java.util.UUID
import com.example.citygrid.data.SupabaseManager
import io.github.jan.supabase.realtime.realtime

object MqttManager {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Flujos de estado internos mutables
    private val _residuosFlow = MutableStateFlow(
        ResiduosState(
            contenedores = listOf(
                ContenedorData("Plástico", "Plastico", 10, System.currentTimeMillis()),
                ContenedorData("Inorgánico", "Inorganico", 10, System.currentTimeMillis()),
                ContenedorData("Orgánico", "Organico", 10, System.currentTimeMillis())
            ),
            conectado = false,
            ultimoMensajeTimestamp = System.currentTimeMillis()
        )
    )
    val residuosFlow: StateFlow<ResiduosState> = _residuosFlow.asStateFlow()

    private val _aguaFlow = MutableStateFlow(AguaState())
    val aguaFlow: StateFlow<AguaState> = _aguaFlow.asStateFlow()

    private val _alumbradoFlow = MutableStateFlow(
        AlumbradoState(
            estadoOn = false,
            condicionNoche = false,
            ldrLux = 500,
            luminariasActivas = 0,
            modo = "AUTO",
            ultimaActualizacion = System.currentTimeMillis(),
            conectado = false
        )
    )
    val alumbradoFlow: StateFlow<AlumbradoState> = _alumbradoFlow.asStateFlow()

    private val _alertasFlow = MutableStateFlow<List<Alerta>>(emptyList())
    val alertasFlow: StateFlow<List<Alerta>> = _alertasFlow.asStateFlow()

    // Métodos públicos para permitir a los procesadores actualizar el estado
    fun updateResiduosState(state: ResiduosState) {
        _residuosFlow.value = state
    }

    fun updateAguaState(state: AguaState) {
        _aguaFlow.value = state
    }

    fun updateAlumbradoState(state: AlumbradoState) {
        _alumbradoFlow.value = state
    }

    fun addMqttAlerta(alerta: Alerta) {
        val listaActual = _alertasFlow.value.toMutableList()
        listaActual.add(0, alerta)
        _alertasFlow.value = listaActual.take(20)
    }

    // Variables de monitoreo de inactividad / fuera de línea y Supabase Realtime
    private var monitorJob: kotlinx.coroutines.Job? = null
    private var ultimoMensajeBasuraTimestamp = System.currentTimeMillis()
    private var esSistemaOnline = true

    @Volatile
    private var isConnecting = false

    // Opciones de conexión MQTT (Requisitos del examen)
    val mqttOptions = MqttConnectOptions().apply {
        userName = Constants.MQTT_USER
        password = Constants.MQTT_PASSWORD.toCharArray()
        isAutomaticReconnect = true
        isCleanSession = false  // CleanSession = false
        connectionTimeout = 30
        keepAliveInterval = 30  // KeepAlive = 30 seg
        socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
    }

    private var client: MqttAsyncClient? = null
    
    // Evita duplicar alertas consecutivas para el mismo contenedor
    private val alertasReportadas = mutableMapOf<String, Boolean>()

    fun connect(context: Context) {
        // Cargar últimos registros históricos de Supabase para no iniciar con valores vacíos (10%)
        cargarLecturasIniciales()

        if (client != null && client!!.isConnected) return
        synchronized(this) {
            if (isConnecting) return
            isConnecting = true
        }

        try {
            val clientId = "CityGrid_Android_" + UUID.randomUUID().toString()
            client = MqttAsyncClient(Constants.MQTT_BROKER_URL, clientId, MemoryPersistence())

            client?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    android.util.Log.d("MqttManager", "Conectado exitosamente a $serverURI. Reconnect: $reconnect")
                    synchronized(this@MqttManager) {
                        isConnecting = false
                    }
                    _residuosFlow.value = _residuosFlow.value.copy(conectado = true)
                    _aguaFlow.value = _aguaFlow.value.copy(conectado = true)
                    _alumbradoFlow.value = _alumbradoFlow.value.copy(conectado = true)
                    suscribirATopics()
                    iniciarMonitorInactividad(context)
                    // Conectar a Supabase Realtime para que otras partes de la app puedan oír alertas en tiempo real
                    scope.launch {
                        try {
                            SupabaseManager.client.realtime.connect()
                        } catch (e: Exception) {
                            android.util.Log.e("MqttManager", "Error al conectar Realtime de Supabase", e)
                        }
                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    android.util.Log.e("MqttManager", "Conexión perdida con el broker", cause)
                    synchronized(this@MqttManager) {
                        isConnecting = false
                    }
                    _residuosFlow.value = _residuosFlow.value.copy(conectado = false)
                    _aguaFlow.value = _aguaFlow.value.copy(conectado = false)
                    _alumbradoFlow.value = _alumbradoFlow.value.copy(conectado = false)
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    val payload = String(message.payload)
                    android.util.Log.d("MqttManager", "Mensaje recibido en $topic: $payload")
                    
                    // Si llega cualquier mensaje de telemetría del ESP32, registrar actividad
                    if (topic == Constants.TOPIC_RESIDUOS ||
                        topic == Constants.TOPIC_AGUA ||
                        topic == Constants.TOPIC_ALUMBRADO
                    ) {
                        registrarActividadDispositivo(context)
                    }

                    parseMessage(topic, payload, context)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })
        } catch (e: Exception) {
            android.util.Log.e("MqttManager", "Error al inicializar MqttAsyncClient", e)
            return
        }

        try {
            client?.connect(mqttOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    android.util.Log.d("MqttManager", "Llamada connect() exitosa")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    android.util.Log.e("MqttManager", "Llamada connect() fallida", exception)
                    synchronized(this@MqttManager) {
                        isConnecting = false
                    }
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("MqttManager", "Error al intentar iniciar la conexión", e)
            synchronized(this) {
                isConnecting = false
            }
        }
    }

    fun disconnect() {
        try {
            monitorJob?.cancel()
            client?.disconnect()
            _residuosFlow.value = _residuosFlow.value.copy(conectado = false)
            _aguaFlow.value = _aguaFlow.value.copy(conectado = false)
            _alumbradoFlow.value = _alumbradoFlow.value.copy(conectado = false)
        } catch (e: Exception) {
            android.util.Log.e("MqttManager", "Error al desconectar", e)
        }
    }

    fun publish(topic: String, payload: String) {
        if (client == null || !client!!.isConnected) {
            android.util.Log.w("MqttManager", "No se puede publicar, cliente desconectado")
            return
        }
        try {
            android.util.Log.d("MqttManager", "Enviando a $topic -> $payload")
            val message = MqttMessage(payload.toByteArray()).apply {
                qos = 1
                isRetained = false
            }
            client?.publish(topic, message)
        } catch (e: Exception) {
            android.util.Log.e("MqttManager", "Error al publicar mensaje", e)
        }
    }

    private fun suscribirATopics() {
        val topics = arrayOf(
            Constants.TOPIC_RESIDUOS,
            Constants.TOPIC_AGUA,
            Constants.TOPIC_ALUMBRADO,
            Constants.TOPIC_ALERTAS
        )
        val qos = IntArray(topics.size) { 1 }

        try {
            client?.subscribe(topics, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    android.util.Log.d("MqttManager", "Suscrito exitosamente a los temas")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    android.util.Log.e("MqttManager", "Fallo al suscribirse a los temas", exception)
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("MqttManager", "Error al suscribirse", e)
        }
    }

    fun parseMessage(topic: String, payload: String, context: Context) {
        scope.launch {
            try {
                when (topic) {
                    // --- Telemetría de Residuos (JSON con contenedor, distancia y porcentaje) ---
                    Constants.TOPIC_RESIDUOS -> {
                        val data = Json.decodeFromString<ResiduoPayload>(payload)
                        val tipo = data.contenedor ?: data.tipo ?: "Plastico"
                        val porcentaje = data.porcentaje ?: data.distancia?.let {
                            (100 - (it / 20.0) * 100).coerceIn(0.0, 100.0).toInt()
                        } ?: 0
                        ResiduosMqttProcessor.procesarMensaje(tipo, porcentaje.toString(), context, scope, data.distancia)
                    }

                    // --- Telemetría de Alumbrado (JSON con ldrLux, estadoOn, etc.) ---
                    Constants.TOPIC_ALUMBRADO -> {
                        AlumbradoMqttProcessor.procesarMensaje(topic, payload, context, scope)
                    }

                    // --- Telemetría de Agua (JSON con nivelTanque y bombaActiva) ---
                    Constants.TOPIC_AGUA -> {
                        AguaMqttProcessor.procesarMensaje(topic, payload, context, scope)
                    }

                    // --- Canal de Alertas Generales ---
                    Constants.TOPIC_ALERTAS -> {
                        AlertasMqttProcessor.procesarAlerta(payload, context)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MqttManager", "Error en parseMessage de topic $topic", e)
            }
        }
    }

    fun cargarLecturasIniciales() {
        ResiduosMqttProcessor.cargarLecturasIniciales(scope)
    }

    private fun iniciarMonitorInactividad(context: Context) {
        monitorJob?.cancel()
        ultimoMensajeBasuraTimestamp = System.currentTimeMillis()
        esSistemaOnline = true

        monitorJob = scope.launch {
            while (true) {
                kotlinx.coroutines.delay(15000) // Verificar cada 15 segundos
                val tiempoInactivo = System.currentTimeMillis() - ultimoMensajeBasuraTimestamp
                if (esSistemaOnline && tiempoInactivo > 120000) { // Mayor a 2 minutos sin recibir ningún dato
                    esSistemaOnline = false

                    // 1. Mostrar notificación de caída
                    NotificationHelper.enviarNotificacion(
                        context = context,
                        tipo = TipoAlerta.ADVERTENCIA,
                        titulo = "Dispositivo Fuera de Línea",
                        mensaje = "Se ha perdido el enlace MQTT con los sensores del ESP32."
                    )

                    // 2. Registrar alerta de inactividad en Supabase
                    val dbAlerta = DbAlerta(
                        idSistema = 1, // Módulo de Residuos
                        idTipoAlerta = 2, // ADVERTENCIA
                        idEstadoAlerta = 1, // PENDIENTE
                        descripcion = "Sin datos de botes de basura por más de 2 minutos — posible falla del dispositivo ESP32 o pérdida de conexión (tiempo inactivo: ${tiempoInactivo / 1000}s)",
                        fechaHora = java.time.OffsetDateTime.now().toString()
                    )
                    AlertaRepository.insertarAlerta(dbAlerta)

                    // 3. Actualizar flujos para mostrar desconexión en la interfaz
                    _residuosFlow.value = _residuosFlow.value.copy(conectado = false, ultimoMensajeTimestamp = ultimoMensajeBasuraTimestamp)
                    _aguaFlow.value = _aguaFlow.value.copy(conectado = false)
                    _alumbradoFlow.value = _alumbradoFlow.value.copy(conectado = false)
                }
            }
        }
    }



    @Synchronized
    private fun registrarActividadDispositivo(context: Context) {
        ultimoMensajeBasuraTimestamp = System.currentTimeMillis()
        _residuosFlow.value = _residuosFlow.value.copy(ultimoMensajeTimestamp = ultimoMensajeBasuraTimestamp)
        if (!esSistemaOnline) {
            esSistemaOnline = true

            // 1. Notificar recuperación de conexión
            NotificationHelper.enviarNotificacion(
                context = context,
                tipo = TipoAlerta.NORMAL,
                titulo = "Dispositivo En Línea",
                mensaje = "Conexión restablecida con el nodo de sensores de CityGrid."
            )

            // 2. Registrar en la lista local y actualizar flujos
            _residuosFlow.value = _residuosFlow.value.copy(conectado = true, ultimoMensajeTimestamp = ultimoMensajeBasuraTimestamp)
            _aguaFlow.value = _aguaFlow.value.copy(conectado = true)
            _alumbradoFlow.value = _alumbradoFlow.value.copy(conectado = true)
        }
    }

    private fun procesarAlertaMqtt(payload: String, context: Context) {
        try {
            // Intenta procesar como JSON estructurado
            val data = Json.decodeFromString<AlertaPayload>(payload)
            val tipoStr = data.tipo ?: "INFORMACION"
            val tipoAlerta = try { TipoAlerta.valueOf(tipoStr.uppercase()) } catch (e: Exception) { TipoAlerta.INFORMACION }
            val titulo = data.titulo ?: "Nueva Alerta de CityGrid"
            val desc = data.descripcion ?: "Se ha recibido un aviso del sistema."
            
            NotificationHelper.enviarNotificacion(context, tipoAlerta, titulo, desc)
            
            // Agregar al listado local de alertas MQTT
            val nuevaAlerta = Alerta(
                id = UUID.randomUUID().toString(),
                tipo = tipoAlerta,
                titulo = titulo,
                descripcion = desc,
                sistema = when (data.idSistema) {
                    1 -> "Residuos"
                    2 -> "Agua"
                    3 -> "Alumbrado"
                    else -> "Sistema"
                },
                timestamp = System.currentTimeMillis(),
                atendida = false
            )
            val listaActual = _alertasFlow.value.toMutableList()
            listaActual.add(0, nuevaAlerta)
            _alertasFlow.value = listaActual.take(20) // Conservar las últimas 20 alertas MQTT
        } catch (e: Exception) {
            // Si no es un JSON, procesar como texto plano (tipo advertencia)
            android.util.Log.d("MqttManager", "Procesando payload de alerta como texto plano: $payload")
            NotificationHelper.enviarNotificacion(context, TipoAlerta.ADVERTENCIA, "Alerta del Sistema", payload)
            
            val nuevaAlerta = Alerta(
                id = UUID.randomUUID().toString(),
                tipo = TipoAlerta.ADVERTENCIA,
                titulo = "Alerta de Dispositivo",
                descripcion = payload,
                sistema = "Sistema",
                timestamp = System.currentTimeMillis(),
                atendida = false
            )
            val listaActual = _alertasFlow.value.toMutableList()
            listaActual.add(0, nuevaAlerta)
            _alertasFlow.value = listaActual.take(20)
        }
    }
}

// Data classes auxiliares para serialización de payloads MQTT
@Serializable
data class ResiduoPayload(
    val contenedor: String? = null,
    val tipo: String? = null,
    val distancia: Double? = null,
    val porcentaje: Int? = null
)

@Serializable
data class AguaPayload(
    val nivelTanque: Int? = null,
    val bombaActiva: Boolean? = null
)

@Serializable
data class AlumbradoPayload(
    val estadoOn: Boolean? = null,
    val condicionNoche: Boolean? = null,
    val ldrLux: Int? = null,
    val luminariasActivas: Int? = null,
    val modo: String? = null
)

@Serializable
data class AlertaPayload(
    val idSistema: Int? = null,
    val idTipoAlerta: Int? = null,
    val tipo: String? = null,
    val titulo: String? = null,
    val descripcion: String? = null
)