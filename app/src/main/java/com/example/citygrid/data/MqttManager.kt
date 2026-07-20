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
import com.example.citygrid.utils.AlertaHelper
import com.example.citygrid.utils.Constants
import com.example.citygrid.utils.Logger
import com.example.citygrid.utils.NotificationHelper
import com.example.citygrid.data.local.db.CityGridDatabase
import com.example.citygrid.data.local.db.entity.AguaEntity
import com.example.citygrid.data.local.db.entity.AlertaEntity
import com.example.citygrid.data.local.db.entity.AlumbradoEntity
import com.example.citygrid.data.local.db.entity.ResiduosEntity
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

enum class MqttConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DEVICE_OFFLINE
}

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

    private val _statusFlow = MutableStateFlow(com.example.citygrid.model.StatusState())
    val statusFlow: StateFlow<com.example.citygrid.model.StatusState> = _statusFlow.asStateFlow()

    // Flujo de estado de conexión MQTT y Latencia en vivo
    private val _connectionState = MutableStateFlow(MqttConnectionState.DISCONNECTED)
    val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()

    private val _latenciaMs = MutableStateFlow(0L)
    val latenciaMs: StateFlow<Long> = _latenciaMs.asStateFlow()

    private var lastMessageTimeMs = 0L
    private var dbInstance: CityGridDatabase? = null

    // Métodos públicos para permitir a los procesadores actualizar el estado
    fun updateResiduosState(state: ResiduosState) {
        _residuosFlow.value = state
        dbInstance?.let { db ->
            scope.launch {
                try {
                    val entities = state.contenedores.map {
                        ResiduosEntity(
                            tipo = it.tipo,
                            nombre = it.nombre,
                            porcentaje = it.porcentaje,
                            ultimaActualizacion = it.ultimaActualizacion
                        )
                    }
                    db.residuosDao().insertarOActualizar(entities)
                } catch (e: Exception) {
                    Logger.e("MqttManager", "Error al guardar residuos en Room DB", e)
                }
            }
        }
    }

    fun updateAguaState(state: AguaState) {
        _aguaFlow.value = state
        dbInstance?.let { db ->
            scope.launch {
                try {
                    db.aguaDao().guardarAgua(
                        AguaEntity(
                            id = 1,
                            nivelTanque = state.nivelTanque,
                            bombaActiva = state.bombaActiva,
                            estadoGeneral = state.estadoGeneral,
                            ultimaActualizacion = state.ultimaActualizacion,
                            conectado = state.conectado
                        )
                    )
                } catch (e: Exception) {
                    Logger.e("MqttManager", "Error al guardar agua en Room DB", e)
                }
            }
        }
    }

    fun updateAlumbradoState(state: AlumbradoState) {
        _alumbradoFlow.value = state
        dbInstance?.let { db ->
            scope.launch {
                try {
                    db.alumbradoDao().guardarAlumbrado(
                        AlumbradoEntity(
                            id = 1,
                            estadoOn = state.estadoOn,
                            condicionNoche = state.condicionNoche,
                            ldrLux = state.ldrLux,
                            luminariasActivas = state.luminariasActivas,
                            modo = state.modo,
                            ultimaActualizacion = state.ultimaActualizacion,
                            conectado = state.conectado
                        )
                    )
                } catch (e: Exception) {
                    Logger.e("MqttManager", "Error al guardar alumbrado en Room DB", e)
                }
            }
        }
    }

    fun addMqttAlerta(alerta: Alerta) {
        val listaActual = _alertasFlow.value.toMutableList()
        listaActual.add(0, alerta)
        _alertasFlow.value = listaActual.take(20)
        dbInstance?.let { db ->
            scope.launch {
                try {
                    db.alertaDao().guardarAlerta(
                        AlertaEntity(
                            id = alerta.id,
                            tipoName = alerta.tipo.name,
                            titulo = alerta.titulo,
                            descripcion = alerta.descripcion,
                            sistema = alerta.sistema,
                            timestamp = alerta.timestamp,
                            atendida = alerta.atendida
                        )
                    )
                } catch (e: Exception) {
                    Logger.e("MqttManager", "Error al guardar alerta en Room DB", e)
                }
            }
        }
    }

    private fun initRoomDatabase(context: Context) {
        if (dbInstance == null) {
            val db = CityGridDatabase.getInstance(context)
            dbInstance = db
            scope.launch {
                try {
                    val cachedResiduos = db.residuosDao().obtenerTodosList()
                    if (cachedResiduos.isNotEmpty()) {
                        val contenedores = cachedResiduos.map {
                            ContenedorData(
                                nombre = it.nombre,
                                tipo = it.tipo,
                                porcentaje = it.porcentaje,
                                ultimaActualizacion = it.ultimaActualizacion
                            )
                        }
                        _residuosFlow.value = _residuosFlow.value.copy(contenedores = contenedores)
                    }

                    val cachedAgua = db.aguaDao().obtenerAgua()
                    if (cachedAgua != null) {
                        _aguaFlow.value = AguaState(
                            nivelTanque = cachedAgua.nivelTanque,
                            bombaActiva = cachedAgua.bombaActiva,
                            estadoGeneral = cachedAgua.estadoGeneral,
                            ultimaActualizacion = cachedAgua.ultimaActualizacion,
                            conectado = cachedAgua.conectado
                        )
                    }

                    val cachedAlumbrado = db.alumbradoDao().obtenerAlumbrado()
                    if (cachedAlumbrado != null) {
                        _alumbradoFlow.value = AlumbradoState(
                            estadoOn = cachedAlumbrado.estadoOn,
                            condicionNoche = cachedAlumbrado.condicionNoche,
                            ldrLux = cachedAlumbrado.ldrLux,
                            luminariasActivas = cachedAlumbrado.luminariasActivas,
                            modo = cachedAlumbrado.modo,
                            ultimaActualizacion = cachedAlumbrado.ultimaActualizacion,
                            conectado = cachedAlumbrado.conectado
                        )
                    }

                    val cachedAlertas = db.alertaDao().obtenerAlertas()
                    if (cachedAlertas.isNotEmpty()) {
                        val alertas = cachedAlertas.map { entity ->
                            val tipoParsed = try {
                                com.example.citygrid.model.TipoAlerta.valueOf(entity.tipoName)
                            } catch (e: Exception) {
                                com.example.citygrid.model.TipoAlerta.INFORMACION
                            }
                            Alerta(
                                id = entity.id,
                                tipo = tipoParsed,
                                titulo = entity.titulo,
                                descripcion = entity.descripcion,
                                sistema = entity.sistema,
                                timestamp = entity.timestamp,
                                atendida = entity.atendida
                            )
                        }
                        _alertasFlow.value = alertas
                    }
                } catch (e: Exception) {
                    Logger.e("MqttManager", "Error al cargar Room DB cache", e)
                }
            }
        }
    }

    // Variables de monitoreo de inactividad / fuera de línea y Supabase Realtime
    private var monitorJob: kotlinx.coroutines.Job? = null
    private var ultimoMensajeBasuraTimestamp = System.currentTimeMillis()
    @Volatile
    private var esSistemaOnline = true
    private val json = Json { ignoreUnknownKeys = true }
    private var retryJob: kotlinx.coroutines.Job? = null
    private var lecturasInicialesCargadas = false

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

    // alertasReportadas movido a AlertaHelper centralizado

    fun connect(context: Context) {
        initRoomDatabase(context)

        // Cargar últimos registros históricos de Supabase solo una vez al inicio
        synchronized(this) {
            if (!lecturasInicialesCargadas) {
                lecturasInicialesCargadas = true
                cargarLecturasIniciales()
            }
        }

        if (client != null && client!!.isConnected) {
            _connectionState.value = MqttConnectionState.CONNECTED
            return
        }
        synchronized(this) {
            if (isConnecting) return
            isConnecting = true
        }
        _connectionState.value = MqttConnectionState.CONNECTING

        try {
            if (client == null) {
                val clientId = "CityGrid_Android_" + UUID.randomUUID().toString()
                client = MqttAsyncClient(Constants.MQTT_BROKER_URL, clientId, MemoryPersistence())

                client?.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                        Logger.d("MqttManager", "Conectado exitosamente a $serverURI. Reconnect: $reconnect")
                        synchronized(this@MqttManager) {
                            isConnecting = false
                            retryJob?.cancel()
                        }
                        _connectionState.value = MqttConnectionState.CONNECTED
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
                                Logger.e("MqttManager", "Error al conectar Realtime de Supabase", e)
                            }
                        }
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Logger.e("MqttManager", "Conexión perdida con el broker", cause)
                        synchronized(this@MqttManager) {
                            isConnecting = false
                        }
                        _connectionState.value = MqttConnectionState.DISCONNECTED
                        _residuosFlow.value = _residuosFlow.value.copy(conectado = false)
                        _aguaFlow.value = _aguaFlow.value.copy(conectado = false)
                        _alumbradoFlow.value = _alumbradoFlow.value.copy(conectado = false)
                    }

                    override fun messageArrived(topic: String, message: MqttMessage) {
                        val now = System.currentTimeMillis()
                        if (lastMessageTimeMs > 0) {
                            val delta = now - lastMessageTimeMs
                            if (delta in 5..30000) {
                                _latenciaMs.value = delta
                            }
                        }
                        lastMessageTimeMs = now

                        val payload = String(message.payload)
                        Logger.d("MqttManager", "Mensaje recibido en $topic: $payload")

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
            }
        } catch (e: Exception) {
            Logger.e("MqttManager", "Error al inicializar MqttAsyncClient", e)
            synchronized(this) {
                isConnecting = false
            }
            _connectionState.value = MqttConnectionState.DISCONNECTED
            reintentarConexion(context)
            return
        }

        try {
            client?.connect(mqttOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Logger.d("MqttManager", "Llamada connect() exitosa")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Logger.e("MqttManager", "Llamada connect() fallida", exception)
                    synchronized(this@MqttManager) {
                        isConnecting = false
                    }
                    reintentarConexion(context)
                }
            })
        } catch (e: Exception) {
            Logger.e("MqttManager", "Error al intentar iniciar la conexión", e)
            synchronized(this) {
                isConnecting = false
            }
            reintentarConexion(context)
        }
    }

    private fun reintentarConexion(context: Context) {
        synchronized(this) {
            if (retryJob?.isActive == true) return
            retryJob = scope.launch {
                Logger.d("MqttManager", "Programando reintento de conexión MQTT en 5 segundos...")
                kotlinx.coroutines.delay(5000)
                if (client == null || !client!!.isConnected) {
                    Logger.d("MqttManager", "Reintentando conectar MQTT...")
                    connect(context)
                }
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
            Logger.e("MqttManager", "Error al desconectar", e)
        }
    }

    fun publish(topic: String, payload: String) {
        if (client == null || !client!!.isConnected) {
            Logger.w("MqttManager", "No se puede publicar, cliente desconectado")
            return
        }
        try {
            Logger.d("MqttManager", "Enviando a $topic -> $payload")
            val message = MqttMessage(payload.toByteArray()).apply {
                qos = 1
                isRetained = false
            }
            client?.publish(topic, message)
        } catch (e: Exception) {
            Logger.e("MqttManager", "Error al publicar mensaje", e)
        }
    }

    private fun suscribirATopics() {
        val topics = arrayOf(
            Constants.TOPIC_RESIDUOS,
            Constants.TOPIC_AGUA,
            Constants.TOPIC_ALUMBRADO,
            Constants.TOPIC_ALERTAS,
            Constants.TOPIC_STATUS
        )
        val qos = IntArray(topics.size) { 1 }

        try {
            client?.subscribe(topics, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Logger.d("MqttManager", "Suscrito exitosamente a los temas")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Logger.e("MqttManager", "Fallo al suscribirse a los temas", exception)
                }
            })
        } catch (e: Exception) {
            Logger.e("MqttManager", "Error al suscribirse", e)
        }
    }

    fun parseMessage(topic: String, payload: String, context: Context) {
        scope.launch {
            try {
                when (topic) {
                    // --- Telemetría de Residuos (JSON con contenedor, distancia y porcentaje) ---
                    Constants.TOPIC_RESIDUOS -> {
                        val data = json.decodeFromString<ResiduoPayload>(payload)
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

                    // --- Diagnóstico de Estado / Heartbeat ---
                    Constants.TOPIC_STATUS -> {
                        procesarStatus(payload, context)
                    }
                }
            } catch (e: Exception) {
                Logger.e("MqttManager", "Error en parseMessage de topic $topic", e)
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
                    _connectionState.value = MqttConnectionState.DEVICE_OFFLINE

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
            _connectionState.value = MqttConnectionState.CONNECTED

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

    private fun procesarStatus(payload: String, context: Context) {
        try {
            val status = json.decodeFromString<com.example.citygrid.model.StatusState>(payload)
            _statusFlow.value = status

            if (status.estado.equals("OFFLINE", ignoreCase = true)) {
                Logger.w("MqttManager", "Dispositivo ESP32 reportó desconexión abrupta (LWT)")
                esSistemaOnline = false
                _connectionState.value = MqttConnectionState.DEVICE_OFFLINE

                // Actualizar flujos para mostrar desconexión en la interfaz
                val resState = _residuosFlow.value
                val contenedoresActualizados = resState.contenedores.map { it.copy(activo = false) }
                _residuosFlow.value = resState.copy(contenedores = contenedoresActualizados, conectado = false)
                _aguaFlow.value = _aguaFlow.value.copy(conectado = false)
                _alumbradoFlow.value = _alumbradoFlow.value.copy(conectado = false)

                // Notificar caída inmediata
                NotificationHelper.enviarNotificacion(
                    context = context,
                    tipo = TipoAlerta.ADVERTENCIA,
                    titulo = "Dispositivo Fuera de Línea",
                    mensaje = "Se ha perdido la conexión de red con el dispositivo ESP32."
                )

                // Registrar alerta en Supabase
                scope.launch {
                    val dbAlerta = DbAlerta(
                        idSistema = 1, // General
                        idTipoAlerta = 2, // ADVERTENCIA
                        idEstadoAlerta = 1, // PENDIENTE
                        descripcion = "Conexión LWT perdida con el dispositivo ESP32.",
                        fechaHora = java.time.OffsetDateTime.now().toString()
                    )
                    AlertaRepository.insertarAlerta(dbAlerta)
                }
            } else {
                // Registrar actividad si llega reporte de estado activo (ONLINE)
                registrarActividadDispositivo(context)

                // Evaluar salud de cada sensor físico individual
                verificarSaludSensor(status.sensores.basPlastico, "Contenedor de Plástico", 1, context)
                verificarSaludSensor(status.sensores.basInorganico, "Contenedor de Inorgánico", 1, context)
                verificarSaludSensor(status.sensores.basOrganico, "Contenedor de Orgánico", 1, context)
                verificarSaludSensor(status.sensores.nivelAgua, "Sensor del Depósito de Agua", 2, context)
                verificarSaludSensor(status.sensores.ldrLuz, "Fotoresistencia LDR (Alumbrado)", 3, context)

                // Actualizar el estado de conexión individual en cada flujo
                val plasticoActivo = !status.sensores.basPlastico.equals("ERROR_DESCONECTADO", ignoreCase = true)
                val inorganicoActivo = !status.sensores.basInorganico.equals("ERROR_DESCONECTADO", ignoreCase = true)
                val organicoActivo = !status.sensores.basOrganico.equals("ERROR_DESCONECTADO", ignoreCase = true)
                val aguaActivo = !status.sensores.nivelAgua.equals("ERROR_DESCONECTADO", ignoreCase = true)
                val ldrActivo = !status.sensores.ldrLuz.equals("ERROR_DESCONECTADO", ignoreCase = true)

                val resState = _residuosFlow.value
                val contenedoresActualizados = resState.contenedores.map { contenedor ->
                    when (contenedor.tipo.lowercase()) {
                        "plastico" -> contenedor.copy(activo = plasticoActivo)
                        "inorganico" -> contenedor.copy(activo = inorganicoActivo)
                        "organico" -> contenedor.copy(activo = organicoActivo)
                        else -> contenedor
                    }
                }
                _residuosFlow.value = resState.copy(contenedores = contenedoresActualizados, conectado = true)
                _aguaFlow.value = _aguaFlow.value.copy(conectado = aguaActivo)
                _alumbradoFlow.value = _alumbradoFlow.value.copy(conectado = ldrActivo)
            }
        } catch (e: Exception) {
            Logger.e("MqttManager", "Error al procesar mensaje de Status: $payload", e)
        }
    }

    private fun verificarSaludSensor(estado: String, nombreSensor: String, idSistema: Int, context: Context) {
        val claveAlerta = "sensor_fallo_${nombreSensor.replace(" ", "_")}"
        if (estado.equals("ERROR_DESCONECTADO", ignoreCase = true)) {
            scope.launch {
                AlertaHelper.emitirSiNueva(
                    clave = claveAlerta,
                    tipo = TipoAlerta.CRITICO,
                    titulo = "Fallo de Hardware: $nombreSensor",
                    mensaje = "El sensor físico de $nombreSensor está desconectado o averiado. Requiere mantenimiento.",
                    dbAlerta = DbAlerta(
                        idSistema = idSistema,
                        idTipoAlerta = 1,
                        idEstadoAlerta = 1,
                        descripcion = "Fallo de Hardware detectado en: $nombreSensor (Sensor desconectado/averiado)",
                        fechaHora = java.time.OffsetDateTime.now().toString()
                    ),
                    context = context
                )
            }
        } else {
            if (AlertaHelper.estaReportada(claveAlerta)) {
                AlertaHelper.liberar(claveAlerta)
                NotificationHelper.enviarNotificacion(
                    context = context,
                    tipo = TipoAlerta.NORMAL,
                    titulo = "Sensor Recuperado",
                    mensaje = "El sensor físico de $nombreSensor ha vuelto a operar correctamente."
                )
                scope.launch {
                    AlertaRepository.insertarAlerta(
                        DbAlerta(
                            idSistema = idSistema,
                            idTipoAlerta = 4,
                            idEstadoAlerta = 2,
                            descripcion = "Sensor $nombreSensor recuperado con éxito",
                            fechaHora = java.time.OffsetDateTime.now().toString()
                        )
                    )
                }
            }
        }
    }

    /** Libera recursos del MqttManager. Llamar desde onDestroy de la Activity. */
    fun shutdown() {
        monitorJob?.cancel()
        retryJob?.cancel()
        try {
            client?.disconnect()
        } catch (_: Exception) {}
        client?.close()
        client = null
        AlertaHelper.limpiarTodas()
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
