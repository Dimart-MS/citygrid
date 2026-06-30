package com.example.citygrid.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.citygrid.R
import com.example.citygrid.data.SessionManager
import com.example.citygrid.data.repository.UsuarioRepository
import com.example.citygrid.ui.theme.SurfaceCard
import kotlinx.coroutines.launch

/**
 * Módulo 1 — Inicio de Sesión.
 *
 * Flujo de autenticación:
 * 1. Valida formato local (campos vacíos, "@" obligatorio).
 * 2. Intenta login contra Supabase (tabla 'usuarios') verificando el
 *    password con BCrypt. No existe fallback offline: si no hay conexión
 *    o las credenciales son inválidas, se muestra el error al usuario.
 * 3. Al éxito: guarda sesión en SharedPreferences + navega al Dashboard.
 */
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Seed del admin la primera vez que se abre la pantalla.
    // LaunchedEffect ya provee un scope de corrutina: no hace falta anidar.
    LaunchedEffect(Unit) {
        UsuarioRepository.seedAdmin()
    }

    // Logo: usa R.drawable.logosinfondo si existe; si no, fallback a GridOn
    val logoExists = runCatching {
        context.resources.getIdentifier("logosinfondo", "drawable", context.packageName)
    }.getOrDefault(0) != 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Logo ────────────────────────────────────────────────────
            if (logoExists) {
                Image(
                    painter = painterResource(id = R.drawable.logosinfondo),
                    contentDescription = "Logo CityGrid",
                    modifier = Modifier.size(96.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.GridOn,
                        contentDescription = "Logo CityGrid",
                        tint = SurfaceCard,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "CityGrid",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sistema Inteligente de Gestión Sustentable",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(40.dp))

            // ── Encabezado del formulario ───────────────────────────────
            Text(
                text = "Inicio de sesión",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Ingresa tus credenciales para acceder al sistema",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // ── Campo correo ────────────────────────────────────────────
            Text(
                text = "Correo electrónico",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it; errorMsg = "" },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ejemplo@correo.com") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            // ── Campo contraseña ────────────────────────────────────────
            Text(
                text = "Contraseña",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMsg = "" },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                                          else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar contraseña"
                                                 else "Mostrar contraseña"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // ── Olvidaste contraseña ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { /* Fase posterior: recuperación con Supabase */ }) {
                    Text("¿Olvidaste tu contraseña?", color = MaterialTheme.colorScheme.primary)
                }
            }

            // ── Error ───────────────────────────────────────────────────
            if (errorMsg.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Botón Iniciar Sesión ────────────────────────────────────
            Button(
                onClick = {
                    // 1. Validaciones locales primero
                    when {
                        correo.isBlank()   -> { errorMsg = "El correo no puede estar vacío"; return@Button }
                        password.isBlank() -> { errorMsg = "La contraseña no puede estar vacía"; return@Button }
                        !correo.contains("@") -> { errorMsg = "Formato de correo inválido"; return@Button }
                    }

                    // 2. Intentar login contra Supabase en una corrutina.
                    //    Sin bypass offline: si no hay conexión o las credenciales
                    //    son inválidas, se muestra el error. No hay fallback local.
                    isLoading = true
                    scope.launch {
                        val result = UsuarioRepository.login(correo, password)
                        result.fold(
                            onSuccess = { usuario ->
                                // Login exitoso contra Supabase
                                sessionManager.guardarSesion(usuario.correo, usuario.nombre, usuario.idUsuario)
                                onLoginSuccess()
                            },
                            onFailure = { error ->
                                errorMsg = error.message ?: "Credenciales incorrectas"
                            }
                        )
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Iniciar Sesión", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(24.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Spacer(Modifier.height(12.dp))

            Text(
                text = "¿No tienes una cuenta? Contacta al administrador.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
