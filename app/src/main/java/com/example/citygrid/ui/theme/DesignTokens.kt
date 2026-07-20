package com.example.citygrid.ui.theme

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Sistema de tokens de diseño CityGrid.
 *
 * Centraliza espaciados, radios, tamaños y animaciones para garantizar
 * consistencia visual en toda la aplicación.
 *
 * Escala de espaciado: 4px base (4, 8, 12, 16, 20, 24, 28, 32, 40, 48)
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 28.dp
    val xxxxl = 32.dp
    val display = 40.dp
    val hero = 48.dp
}

/**
 * Radios de esquina estandarizados.
 */
object Radius {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val full = 100.dp
}

/**
 * Tamaños de íconos estandarizados.
 */
object IconSize {
    val xs = 14.dp
    val sm = 16.dp
    val md = 18.dp
    val lg = 20.dp
    val xl = 22.dp
    val xxl = 24.dp
    val display = 28.dp
    val hero = 32.dp
}

/**
 * Tamaños de avatar/ícono circular estandarizados.
 */
object AvatarSize {
    val sm = 32.dp
    val md = 36.dp
    val lg = 40.dp
    val xl = 48.dp
    val xxl = 56.dp
    val hero = 64.dp
}

/**
 * Duraciones y easing estandarizados para animaciones.
 */
object Motion {
    val durationFast = 150
    val durationNormal = 200
    val durationSlow = 300
    val durationDisplay = 400
    val durationHero = 500

    val easingStandard = FastOutSlowInEasing
    val easingDecelerate = LinearOutSlowInEasing
    val easingAccelerate = FastOutLinearInEasing

    fun <T> tweenFast() = tween<T>(durationMillis = durationFast, easing = easingStandard)
    fun <T> tweenNormal() = tween<T>(durationMillis = durationNormal, easing = easingStandard)
    fun <T> tweenSlow() = tween<T>(durationMillis = durationSlow, easing = easingStandard)
    fun <T> tweenDisplay() = tween<T>(durationMillis = durationDisplay, easing = easingStandard)
}

/**
 * Elevaciones estandarizadas.
 */
object Elevation {
    val none = 0.dp
    val sm = 1.dp
    val md = 2.dp
    val lg = 4.dp
    val xl = 6.dp
    val xxl = 8.dp
    val display = 12.dp
    val hero = 16.dp
}

/**
 * Colores de sombra tintados para usar con Modifier.shadow().
 */
object ShadowColor {
    val card = ShadowColorLight
    val header = Color(0x330F1B2D)
    val button = Color(0x1A2563EB)
}

/**
 * Grosores de borde estandarizados.
 */
object BorderWidth {
    val hairline = 0.5.dp
    val thin = 1.dp
    val medium = 1.5.dp
    val thick = 2.dp
}
