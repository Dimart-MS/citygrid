package com.example.citygrid.ui.login

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.citygrid.R
import com.example.citygrid.data.SessionManager
import com.example.citygrid.data.repository.UsuarioRepository
import com.example.citygrid.ui.theme.CityGridPrimary
import com.example.citygrid.ui.theme.CityGridPrimaryDark
import com.example.citygrid.ui.theme.CityGridPrimaryMid
import com.example.citygrid.ui.theme.SurfaceCard
import com.example.citygrid.ui.theme.TextSecondary
import com.example.citygrid.ui.theme.brandGradient
import com.example.citygrid.ui.theme.Radius
import com.example.citygrid.ui.theme.Spacing
import kotlinx.coroutines.launch

/**
 * Módulo 1 — Inicio de Sesión v2.
 *
 * Flujo de autenticación:
 * 1. Valida formato local (campos vacíos, "@" obligatorio).
 * 2. Intenta login contra Supabase (tabla 'usuarios') verificando el
 *    password con BCrypt.
 * 3. Al éxito: guarda sesión en SharedPreferences + navega al Dashboard.
 */
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Botón scale animation
    var buttonPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (buttonPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "buttonScale"
    )

    LaunchedEffect(Unit) {
        UsuarioRepository.seedAdmin()
    }

    val logoExists = runCatching {
        context.resources.getIdentifier("logosinfondo", "drawable", context.packageName)
    }.getOrDefault(0) != 0

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Fondo con degradado diagonal dramático ──────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(colors = brandGradient())
                )
        )

        // ── Patrón decorativo círculos en el fondo ──────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val circleColor = Color.White.copy(alpha = 0.03f)
            drawCircle(color = circleColor, radius = size.width * 0.65f, center = Offset(size.width * 0.85f, size.height * 0.15f))
            drawCircle(color = circleColor, radius = size.width * 0.5f, center = Offset(size.width * 0.1f, size.height * 0.75f))
            drawCircle(color = Color.White.copy(alpha = 0.02f), radius = size.width * 0.3f, center = Offset(size.width * 0.5f, size.height * 0.5f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { keyboardController?.hide() })
                }
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Logo con halo luminoso ──────────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color.White.copy(alpha = 0.06f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color.White.copy(alpha = 0.10f), CircleShape)
                )
                if (logoExists) {
                    Image(
                        painter = painterResource(id = R.drawable.logosinfondo),
                        contentDescription = "Logo CityGrid",
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.GridOn,
                        contentDescription = "Logo CityGrid",
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "CityGrid",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Smart City Platform",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f),
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(40.dp))

            // ── Card del formulario ─────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.xxl, vertical = Spacing.xxxl)
                ) {
                    Text(
                        text = "Inicio de sesión",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ingresa tus credenciales para continuar",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(24.dp))

                    // Campo correo
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it; errorMsg = "" },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Correo electrónico") },
                        placeholder = { Text("ejemplo@correo.com") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Email,
                                contentDescription = null,
                                tint = CityGridPrimary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { keyboardController?.hide() }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(Radius.lg)
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    // Campo contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = "" },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = CityGridPrimary
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                                                  else Icons.Filled.Visibility,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña"
                                                         else "Mostrar contraseña",
                                    tint = CityGridPrimary
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(Radius.lg)
                    )

                    // Olvidé mi contraseña
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { /* Fase posterior: recuperación con Supabase */ }) {
                            Text(
                                "¿Olvidaste tu contraseña?",
                                color = CityGridPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    // Error
                    if (errorMsg.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(Radius.md)
                                )
                                .padding(Spacing.md)
                        ) {
                            Text(
                                text = errorMsg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Botón con gradiente y scale animation ───────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .scale(buttonScale)
                            .clip(RoundedCornerShape(Radius.lg))
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (!isLoading) listOf(
                                        CityGridPrimary,
                                        CityGridPrimaryMid
                                    ) else listOf(
                                        Color.Gray.copy(alpha = 0.4f),
                                        Color.Gray.copy(alpha = 0.4f)
                                    )
                                )
                            )
                            .clickable(enabled = !isLoading) {
                                buttonPressed = true
                                when {
                                    correo.isBlank()       -> { errorMsg = "El correo no puede estar vacío"; buttonPressed = false; return@clickable }
                                    password.isBlank()     -> { errorMsg = "La contraseña no puede estar vacía"; buttonPressed = false; return@clickable }
                                    !correo.contains("@") -> { errorMsg = "Formato de correo inválido"; buttonPressed = false; return@clickable }
                                }
                                isLoading = true
                                scope.launch {
                                    try {
                                        val result = UsuarioRepository.login(correo, password)
                                        result.fold(
                                            onSuccess = { usuario ->
                                                sessionManager.guardarSesion(usuario.correo, usuario.nombre, usuario.idUsuario)
                                                onLoginSuccess()
                                            },
                                            onFailure = { error ->
                                                errorMsg = error.message ?: "Credenciales incorrectas"
                                            }
                                        )
                                    } finally {
                                        isLoading = false
                                        buttonPressed = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                "Iniciar Sesión",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "¿No tienes una cuenta? Contacta al administrador.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f)
            )

            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.height(40.dp))
        }
    }
}
