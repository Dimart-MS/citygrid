# CityGrid — Smart City Platform (Android)

Aplicación Android para el monitoreo y gestión en tiempo real de subsistemas urbanos:
residuos sólidos, suministro de agua, alumbrado público y alertas de mantenimiento.

---

## Stack técnico

| Tecnología | Versión |
|---|---|
| Kotlin | 2.0.21 |
| Jetpack Compose BOM | 2024.12.01 |
| Navigation Compose | 2.8.5 |
| Supabase BOM | 3.0.2 |
| MQTT (Paho) | 1.2.5 |
| Gradle Plugin | 8.7.3 |
| compileSdk / targetSdk | 35 |
| minSdk | 26 (Android 8.0) |
| JVM Target | 17 |

---

## Requisitos previos

- Android Studio **Ladybug Feature Drop (2024.2.2)** o superior
- JDK 17
- Emulador con API 26+ o dispositivo físico con Android 8.0+
- Cuenta en [Supabase](https://supabase.com) con el proyecto configurado

---

## Cómo arrancar

```bash
# 1. Clonar el repositorio
git clone https://github.com/Dimart-MS/citygrid.git
cd citygrid-android

# 2. Jalar la rama base (Fer y Omar: empiecen aquí)
git checkout base/setup
git pull origin base/setup

# 3. Crear tu rama de feature
git checkout -b feature/<tu-nombre>-<modulo>
# Ejemplos:
#   feature/fer-mqtt-residuos-alertas
#   feature/omar-alumbrado-agua-mantenimiento
```

Abre el proyecto en Android Studio → **Build → Make Project** → debe compilar sin errores.

---

## Configuración de Supabase

Crea el archivo `app/src/main/res/values/secrets.xml` (está en `.gitignore`, **no lo subas**):

```xml
<resources>
    <string name="supabase_url">https://xxxxxxxxxxxx.supabase.co</string>
    <string name="supabase_anon_key">tu_anon_key_aquí</string>
</resources>
```

Veremos aun las credenciales para este.

---

## Estructura del proyecto

```
com.citygrid.app/
│
├── MainActivity.kt
│
├── ui/
│   ├── theme/               → Paleta de colores, tipografía y tema Material3
│   ├── components/          → Componentes reutilizables (TopBar, Badge, Chart, AlertCard)
│   ├── login/               → Módulo 1: Login con Supabase Auth
│   ├── dashboard/           → Módulo 2: Dashboard general (Diego)
│   ├── residuos/            → Módulo 3: Gestión de residuos (Fer)
│   ├── alertas/             → Módulo 4: Historial de alertas (Fer)
│   ├── alumbrado/           → Módulo 5: Alumbrado público (Omar)
│   ├── agua/                → Módulo 6: Suministro de agua (Omar)
│   └── mantenimiento/       → Módulo 7: Mantenimiento (Omar)
│
├── navigation/
│   ├── Routes.kt            → Rutas type-safe (sealed class)
│   ├── BottomNavItems.kt    → Ítems de la barra de navegación inferior
│   └── NavGraph.kt          → NavHost + Scaffold con BottomBar condicional
│
├── model/                   → Data classes compartidas por todos los módulos
│   ├── Usuario.kt
│   ├── Contenedor.kt
│   ├── Alerta.kt            → Incluye enum TipoAlerta
│   ├── ResiduosState.kt
│   ├── AguaState.kt
│   ├── AlumbradoState.kt
│   └── Mantenimiento.kt
│
├── data/
│   ├── SessionManager.kt    → SharedPreferences: sesión del usuario
│   └── MqttManager.kt      → StateFlows de MQTT (Fer implementa el interior)
│
└── utils/
    └── Constants.kt         → Topics MQTT, claves SharedPreferences, umbrales
```

---

## Módulos y responsables

| Módulo | Pantallas | Responsable | Rama |
|---|---|---|---|
| Arquitectura base, Login, Dashboard | `LoginScreen`, `DashboardScreen` | **Diego** | `base/setup` |
| Residuos + Alertas | `ResiduosScreen`, `AlertasScreen` | **Fer** | `feature/fer-mqtt-residuos-alertas` |
| Alumbrado + Agua + Mantenimiento | `AlumbradoScreen`, `AguaScreen`, `MantenimientoScreen` | **Omar** | `feature/omar-alumbrado-agua-mantenimiento` |

> Fer y Omar: sus pantallas ya existen como placeholders en `base/setup`. Solo reemplácenlas con la implementación real.

---

## Flujo de ramas Git

```
base/setup  (Diego — base que todos jalan)
    ├── feature/fer-mqtt-residuos-alertas
    └── feature/omar-alumbrado-agua-mantenimiento
```

No hagan merge directo a `base/setup` sin avisar.

---

## Comunicación entre subsistemas (MQTT)

Los datos de sensores llegan por MQTT desde los ESP32. No llamar directamente a los topics — consumir siempre los `StateFlow` de `MqttManager`:

| StateFlow | Topic | Responsable |
|---|---|---|
| `residuosFlow` | `citygrid/residuos` | Fer |
| `aguaFlow` | `citygrid/agua` | Omar |
| `alumbradoFlow` | `citygrid/alumbrado` | Omar |
| `alertasFlow` | `citygrid/alertas` | Diego / compartido |

Broker: `tcp://broker.hivemq.com:1883`

---

## Convenciones

- Screens: `@Composable fun NombreScreen()` en su propio archivo y paquete
- ViewModels: un ViewModel por pantalla, mismo paquete que la Screen
- Colores: usar siempre las constantes de `Color.kt`, nunca `Color(0xFF...)` inline
- Strings hardcodeados en español están permitidos en esta etapa; no se usa `strings.xml` por ahora

---

## Credenciales de prueba (solo desarrollo)

| Campo | Valor |
|---|---|
| Correo | `admin@citygrid.com` |
| Contraseña | `citygrid123` |

> Estas credenciales son solo para pruebas locales. No usar en producción.