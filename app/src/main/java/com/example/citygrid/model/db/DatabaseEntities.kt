package com.example.citygrid.model.db

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =============================================================================
// Roles & Usuarios
// =============================================================================

@Serializable
data class DbRol(
    @SerialName("IdRol") val idRol: Int? = null,
    @SerialName("Nombre") val nombre: String,
    @SerialName("Descripcion") val descripcion: String? = null
)

@Serializable
data class DbUsuario(
    @SerialName("IdUsuario") val idUsuario: Int? = null,
    @SerialName("IdRol") val idRol: Int,
    @SerialName("Nombre") val nombre: String,
    @SerialName("Correo") val correo: String,
    @SerialName("PasswordHash") val passwordHash: String,
    @SerialName("Estado") val estado: Boolean = true,
    @SerialName("FechaRegistro") val fechaRegistro: String? = null
)

// =============================================================================
// Módulo de Residuos
// =============================================================================

@Serializable
data class DbTipoResiduo(
    @SerialName("IdTipoResiduo") val idTipoResiduo: Int? = null,
    @SerialName("Nombre") val nombre: String
)

@Serializable
data class DbContenedor(
    @SerialName("IdContenedor") val idContenedor: Int? = null,
    @SerialName("IdTipoResiduo") val idTipoResiduo: Int,
    @SerialName("Nombre") val nombre: String,
    @SerialName("AlturaCM") val alturaCm: Double,
    @SerialName("CapacidadLitros") val capacidadLitros: Double,
    @SerialName("Estado") val estado: Boolean = true
)

@Serializable
data class DbLecturaResiduo(
    @SerialName("IdLecturaResiduo") val idLecturaResiduo: Long? = null,
    @SerialName("IdContenedor") val idContenedor: Int,
    @SerialName("DistanciaCM") val distanciaCm: Double,
    @SerialName("NivelLlenado") val nivelLlenado: Double,
    @SerialName("FechaHora") val fechaHora: String? = null
)

// =============================================================================
// Módulo de Agua
// =============================================================================

@Serializable
data class DbTanque(
    @SerialName("IdTanque") val idTanque: Int? = null,
    @SerialName("Nombre") val nombre: String,
    @SerialName("CapacidadLitros") val capacidadLitros: Double,
    @SerialName("AlturaCM") val alturaCm: Double,
    @SerialName("Estado") val estado: Boolean = true
)

@Serializable
data class DbBomba(
    @SerialName("IdBomba") val idBomba: Int? = null,
    @SerialName("Nombre") val nombre: String,
    @SerialName("Estado") val estado: Boolean = true,
    @SerialName("FechaActualizacion") val fechaActualizacion: String? = null
)

@Serializable
data class DbBombaTanque(
    @SerialName("IdBomba") val idBomba: Int,
    @SerialName("IdTanque") val idTanque: Int
)

@Serializable
data class DbLecturaAgua(
    @SerialName("IdLecturaAgua") val idLecturaAgua: Long? = null,
    @SerialName("IdTanque") val idTanque: Int,
    @SerialName("DistanciaCM") val distanciaCm: Double,
    @SerialName("NivelAgua") val nivelAgua: Double,
    @SerialName("FechaHora") val fechaHora: String? = null
)

// =============================================================================
// Módulo de Alumbrado
// =============================================================================

@Serializable
data class DbLuminaria(
    @SerialName("IdLuminaria") val idLuminaria: Int? = null,
    @SerialName("Nombre") val nombre: String,
    @SerialName("Ubicacion") val ubicacion: String? = null,
    @SerialName("Estado") val estado: Boolean = true
)

@Serializable
data class DbLecturaLuminaria(
    @SerialName("IdLecturaLuminaria") val idLecturaLuminaria: Long? = null,
    @SerialName("IdLuminaria") val idLuminaria: Int,
    @SerialName("ValorLDR") val valorLdr: Int,
    @SerialName("FechaHora") val fechaHora: String? = null
)

// =============================================================================
// Dispositivos y Alertas
// =============================================================================

@Serializable
data class DbTipoDispositivo(
    @SerialName("IdTipoDispositivo") val idTipoDispositivo: Int? = null,
    @SerialName("Nombre") val nombre: String
)

@Serializable
data class DbDispositivoIot(
    @SerialName("IdDispositivo") val idDispositivo: Int? = null,
    @SerialName("IdTipoDispositivo") val idTipoDispositivo: Int,
    @SerialName("Nombre") val nombre: String,
    @SerialName("EstadoConexion") val estadoConexion: String,
    @SerialName("UltimaComunicacion") val ultimaComunicacion: String? = null
)

@Serializable
data class DbSistema(
    @SerialName("IdSistema") val idSistema: Int? = null,
    @SerialName("Nombre") val nombre: String
)

@Serializable
data class DbTipoAlerta(
    @SerialName("IdTipoAlerta") val idTipoAlerta: Int? = null,
    @SerialName("Nombre") val nombre: String
)

@Serializable
data class DbEstadoAlerta(
    @SerialName("IdEstadoAlerta") val idEstadoAlerta: Int? = null,
    @SerialName("Nombre") val nombre: String
)

@Serializable
data class DbAlerta(
    @SerialName("IdAlerta") val idAlerta: Long? = null,
    @SerialName("IdSistema") val idSistema: Int,
    @SerialName("IdTipoAlerta") val idTipoAlerta: Int,
    @SerialName("IdEstadoAlerta") val idEstadoAlerta: Int,
    @SerialName("Descripcion") val descripcion: String,
    @SerialName("FechaHora") val fechaHora: String? = null
)

@Serializable
data class DbNotificacion(
    @SerialName("IdNotificacion") val idNotificacion: Long? = null,
    @SerialName("IdAlerta") val idAlerta: Long,
    @SerialName("Titulo") val titulo: String,
    @SerialName("Mensaje") val mensaje: String,
    @SerialName("FechaEnvio") val fechaEnvio: String? = null,
    @SerialName("Leida") val leida: Boolean = false
)

// =============================================================================
// Mantenimiento y Bitácora
// =============================================================================

@Serializable
data class DbComponente(
    @SerialName("IdComponente") val idComponente: Int? = null,
    @SerialName("IdSistema") val idSistema: Int,
    @SerialName("Nombre") val nombre: String
)

@Serializable
data class DbMantenimiento(
    @SerialName("IdMantenimiento") val idMantenimiento: Long? = null,
    @SerialName("IdComponente") val idComponente: Int,
    @SerialName("IdUsuario") val idUsuario: Int,
    @SerialName("Descripcion") val descripcion: String,
    @SerialName("FechaMantenimiento") val fechaMantenimiento: String
)

@Serializable
data class DbBitacoraSistema(
    @SerialName("IdBitacora") val idBitacora: Long? = null,
    @SerialName("IdUsuario") val idUsuario: Int,
    @SerialName("Accion") val accion: String,
    @SerialName("Descripcion") val descripcion: String? = null,
    @SerialName("FechaHora") val fechaHora: String? = null
)
