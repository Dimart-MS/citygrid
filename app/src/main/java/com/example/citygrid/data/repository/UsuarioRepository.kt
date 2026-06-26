package com.example.citygrid.data.repository

import com.example.citygrid.model.db.DbUsuario

object UsuarioRepository {
    /**
     * Inicia sesión verificando credenciales en la tabla 'usuarios'.
     */
    suspend fun login(correo: String, passwordHash: String): Result<DbUsuario> {
        return try {
            // TODO: Implementar consulta a Supabase:
            // val user = SupabaseManager.client.from("usuarios").select { ... }.decodeSingle<DbUsuario>()
            Result.failure(NotImplementedError("Falta implementar login con Supabase"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra un nuevo usuario en la tabla 'usuarios'.
     */
    suspend fun registrar(nombre: String, correo: String, passwordHash: String, idRol: Int): Result<DbUsuario> {
        return try {
            // TODO: Implementar inserción en Supabase
            Result.failure(NotImplementedError("Falta implementar registro con Supabase"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
