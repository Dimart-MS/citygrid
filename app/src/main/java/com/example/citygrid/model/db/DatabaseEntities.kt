package com.example.citygrid.model.db

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =============================================================================
// Roles & Usuarios
// =============================================================================

@Serializable
data class DbRol(
    @SerialName("idrol") val idRol: Int? = null,
    @SerialName("nombre") val nombre: String,
    @SerialName("descripcion") val descripcion: String? = null
)

@Serializable
data class DbUsuario(
    @SerialName("idusuario") val idUsuario: Int? = null,
    @SerialName("idrol") val idRol: Int,
    @SerialName("nombre") val nombre: String,
    @SerialName("correo") val correo: String,
    @SerialName("passwordhash") val passwordHash: String,
    @SerialName("estado") val estado: Boolean = true,
    @SerialName("fecharegistro") val fechaRegistro: String? = null
)

// =============================================================================
// Módulo de Residuos
// =============================================================================


@Serializable
data class DbContenedor(
    @SerialName("idcontenedor") val idContenedor: Int? = null,
    @SerialName("idtiporesiduo") val idTipoResiduo: Int,
    @SerialName("nombre") val nombre: String,
    @SerialName("alturacm") val alturaCm: Double,
    @SerialName("capacidadlitros") val capacidadLitros: Double,
    @SerialName("estado") val estado: Boolean = true
)

@Serializable
data class DbLecturaResiduo(
    @SerialName("idlecturaresiduo") val idLecturaResiduo: Long? = null,
    @SerialName("idcontenedor") val idContenedor: Int,
    @SerialName("distanciacm") val distanciaCm: Double,
    @SerialName("nivelllenado") val nivelLlenado: Double,
    @SerialName("fechahora") val fechaHora: String? = null
)

// =============================================================================
// Módulo de Agua
// =============================================================================

@Serializable
data class DbTanque(
    @SerialName("idtanque") val idTanque: Int? = null,
    @SerialName("nombre") val nombre: String,
    @SerialName("capacidadlitros") val capacidadLitros: Double,
    @SerialName("alturacm") val alturaCm: Double,
    @SerialName("estado") val estado: Boolean = true
)

@Serializable
data class DbBomba(
    @SerialName("idbomba") val idBomba: Int? = null,
    @SerialName("nombre") val nombre: String,
    @SerialName("estado") val estado: Boolean = true,
    @SerialName("fechaactualizacion") val fechaActualizacion: String? = null
)


@Serializable
data class DbLecturaAgua(
    @SerialName("idlecturaagua") val idLecturaAgua: Long? = null,
    @SerialName("idtanque") val idTanque: Int,
    @SerialName("distanciacm") val distanciaCm: Double,
    @SerialName("nivelagua") val nivelAgua: Double,
    @SerialName("fechahora") val fechaHora: String? = null
)

// =============================================================================
// Módulo de Alumbrado
// =============================================================================

@Serializable
data class DbLuminaria(
    @SerialName("idluminaria") val idLuminaria: Int? = null,
    @SerialName("nombre") val nombre: String,
    @SerialName("ubicacion") val ubicacion: String? = null,
    @SerialName("estado") val estado: Boolean = true
)

@Serializable
data class DbLecturaLuminaria(
    @SerialName("idlecturaluminaria") val idLecturaLuminaria: Long? = null,
    @SerialName("idluminaria") val idLuminaria: Int,
    @SerialName("valorldr") val valorLdr: Int,
    @SerialName("fechahora") val fechaHora: String? = null
)

// =============================================================================
// Dispositivos y Alertas
// =============================================================================


@Serializable
data class DbAlerta(
    @SerialName("idalerta") val idAlerta: Long? = null,
    @SerialName("idsistema") val idSistema: Int,
    @SerialName("idtipoalerta") val idTipoAlerta: Int,
    @SerialName("idestadoalerta") val idEstadoAlerta: Int,
    @SerialName("descripcion") val descripcion: String,
    @SerialName("fechahora") val fechaHora: String? = null
)

@Serializable
data class DbNotificacion(
    @SerialName("idnotificacion") val idNotificacion: Long? = null,
    @SerialName("idalerta") val idAlerta: Long,
    @SerialName("titulo") val titulo: String,
    @SerialName("mensaje") val mensaje: String,
    @SerialName("fechaenvio") val fechaEnvio: String? = null,
    @SerialName("leida") val leida: Boolean = false
)

// =============================================================================
// Mantenimiento y Bitácora
// =============================================================================

@Serializable
data class DbComponente(
    @SerialName("idcomponente") val idComponente: Int? = null,
    @SerialName("idsistema") val idSistema: Int,
    @SerialName("nombre") val nombre: String
)

@Serializable
data class DbMantenimiento(
    @SerialName("idmantenimiento") val idMantenimiento: Long? = null,
    @SerialName("idcomponente") val idComponente: Int,
    @SerialName("idusuario") val idUsuario: Int,
    @SerialName("descripcion") val descripcion: String,
    @SerialName("fechamantenimiento") val fechaMantenimiento: String,
    @SerialName("tipo") val tipo: String = "PREVENTIVO" // Nuevo campo
)
@Serializable
data class DbMantenimientoInsert(
    @SerialName("idcomponente") val idComponente: Int,
    @SerialName("idusuario") val idUsuario: Int,
    @SerialName("descripcion") val descripcion: String,
    @SerialName("fechamantenimiento") val fechaMantenimiento: String,
    @SerialName("tipo") val tipo: String
)

@Serializable
data class DbBitacoraSistema(
    @SerialName("idbitacora") val idBitacora: Long? = null,
    @SerialName("idusuario") val idUsuario: Int,
    @SerialName("accion") val accion: String,
    @SerialName("descripcion") val descripcion: String? = null,
    @SerialName("fechahora") val fechaHora: String? = null
)
