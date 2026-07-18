package com.example.citygrid.data.repository

import com.example.citygrid.utils.Logger
import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.model.db.DbRol
import com.example.citygrid.model.db.DbUsuario
import io.github.jan.supabase.postgrest.from
import org.mindrot.jbcrypt.BCrypt

object UsuarioRepository {

    private const val TAG = "UsuarioRepository"

    // ── Credenciales del admin por defecto (solo para seed inicial) ───────
    private const val ADMIN_CORREO      = "admin@citygrid.com"
    private const val ADMIN_PASSWORD     = "citygrid123"
    private const val ADMIN_NOMBRE      = "Administrador"
    private const val ADMIN_ROL         = 1  // idrol = 1 en la tabla 'roles'

    /**
     * Inicia sesión verificando credenciales contra la tabla 'usuarios' en Supabase.
     *
     * Flujo seguro:
     * 1. Busca un usuario donde correo = [correo] (SOLO por correo, nunca por password).
     * 2. Obtiene el hash almacenado de la DB.
     * 3. Requiere formato bcrypt ($2a$ / $2b$ / $2y$); si el hash NO es bcrypt
     *    se rechaza el login (la migración de texto plano la hace seedAdmin()).
     * 4. Usa BCrypt.checkpw() para comparar el password escrito vs el hash.
     *    La verificación es constant-time (resistente a timing attacks).
     * 5. Si coincide y el usuario está activo → login exitoso.
     *
     * El hash NUNCA viaja en la consulta SQL. Solo el correo se envía a Supabase.
     */
    suspend fun login(correo: String, password: String): Result<DbUsuario> {
        return try {
            // Buscar usuario por correo SOLAMENTE (nunca enviamos password a la DB)
            val usuarios = SupabaseManager.client
                .from("usuarios")
                .select {
                    filter { eq("correo", correo) }
                }
                .decodeList<DbUsuario>()

            if (usuarios.isEmpty()) {
                Logger.w(TAG, "No se encontró usuario con correo=$correo")
                return Result.failure(IllegalArgumentException("Credenciales incorrectas"))
            }

            val usuario = usuarios.first()

            // Solo se aceptan hashes bcrypt ($2a$ / $2b$ / $2y$).
            // La migración de texto plano a bcrypt la hace seedAdmin() la
            // primera vez; el login NUNCA compara passwords en texto plano.
            val passwordHash = usuario.passwordHash
            if (!passwordHash.startsWith("\$2")) {
                Logger.w(TAG, "Login rechazado: hash con formato no soportado para correo=$correo")
                return Result.failure(
                    IllegalStateException("Contraseña con formato inválido. Contacta al administrador.")
                )
            }
            val isMatch = BCrypt.checkpw(password, passwordHash)

            if (!isMatch) {
                Logger.w(TAG, "Contraseña incorrecta para correo=$correo")
                return Result.failure(IllegalArgumentException("Credenciales incorrectas"))
            }

            if (!usuario.estado) {
                Logger.w(TAG, "Usuario $correo está desactivado")
                return Result.failure(IllegalStateException("Tu cuenta está desactivada. Contacta al administrador."))
            }

            Logger.d(TAG, "Login exitoso: ${usuario.nombre} (${usuario.correo})")
            Result.success(usuario)

        } catch (e: Exception) {
            Logger.e(TAG, "Error de conexión a Supabase en login", e)
            Result.failure(Exception("Error de conexión. Verifica tu internet."))
        }
    }

    /**
     * Crea el usuario administrador por defecto si no existe.
     *
     * La contraseña se hashea con BCrypt (cost factor 10) antes de insertar.
     * Es idempotente: si admin@citygrid.com ya existe, no hace nada.
     * Soporta migración: si encuentra el admin con password en texto plano,
     * lo actualiza al hash bcrypt automáticamente.
     */
    suspend fun seedAdmin(): Boolean {
        return try {
            // 1. Verificar si ya existe
            val existentes = SupabaseManager.client
                .from("usuarios")
                .select { filter { eq("correo", ADMIN_CORREO) } }
                .decodeList<DbUsuario>()

            if (existentes.isNotEmpty()) {
                val existing = existentes.first()

                // Migración automática: si el password NO es bcrypt, actualizarlo
                if (!existing.passwordHash.startsWith("\$2")) {
                    Logger.w(TAG, "Seed: admin existe con password en texto plano. Migrando a bcrypt...")
                    SupabaseManager.client
                        .from("usuarios")
                        .update(
                            {
                                set("passwordhash", BCrypt.hashpw(ADMIN_PASSWORD, BCrypt.gensalt()))
                            }
                        ) {
                            filter { eq("correo", ADMIN_CORREO) }
                        }
                    Logger.d(TAG, "Seed: password de admin migrado a bcrypt.")
                } else {
                    Logger.d(TAG, "Seed: admin ya existe con bcrypt, no se modifica.")
                }
                return true
            }

            // 2. Verificar que la tabla 'roles' tenga al menos un rol
            val roles = SupabaseManager.client
                .from("roles")
                .select()
                .decodeList<DbRol>()

            if (roles.isEmpty()) {
                SupabaseManager.client
                    .from("roles")
                    .insert(
                        DbRol(
                            nombre = "admin",
                            descripcion = "Administrador del sistema"
                        )
                    )
                Logger.d(TAG, "Seed: rol 'admin' creado.")
            }

            // 3. Crear el usuario admin con password hasheado con bcrypt
            val hashedPassword = BCrypt.hashpw(ADMIN_PASSWORD, BCrypt.gensalt())

            SupabaseManager.client
                .from("usuarios")
                .insert(
                    DbUsuario(
                        idRol = ADMIN_ROL,
                        nombre = ADMIN_NOMBRE,
                        correo = ADMIN_CORREO,
                        passwordHash = hashedPassword,
                        estado = true
                    )
                )

            Logger.d(TAG, "Seed: usuario admin creado con contraseña hasheada (bcrypt).")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Error al hacer seed del admin", e)
            false
        }
    }
}
