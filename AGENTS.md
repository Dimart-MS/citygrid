# CityGrid — AGENTS.md

## Quick start
```bash
./gradlew build
./gradlew test              # unit tests
./gradlew connectedCheck    # instrumented tests (emulator/device needed)
```

## Project structure
- Single module `:app`, package `com.example.citygrid` (≠ `com.citygrid.app` in README)
- **Entry:** `MainActivity.kt` → Compose `Scaffold` + `NavGraph`
- **Routes:** sealed class `Screen` in `navigation/Routes.kt` (Login, Dashboard, Residuos, Agua, Alumbrado, Alertas)
- **Bottom nav:** Home / Residuos / Agua / Alumbrado / Alertas — defined in `BottomNavItems.kt`
- **Auth gate:** `NavGraph.kt` checks `SessionManager.isSesionActiva()` → Dashboard or Login
- **MQTT:** `MqttManager` singleton with `StateFlow` per subsystem (implementation is TODO — Fer)
- **Supabase:** `SupabaseManager` singleton, Postgrest + Realtime installed

## Key config / constants
- **`utils/Constants.kt`** — all hardcoded values (Supabase URL/key, MQTT broker/creds, thresholds, SharedPreferences keys). Secrets are committed here, not in `secrets.xml`.
- **`app/build.gradle.kts`** — `jvmTarget = "11"` (README says 17, trust the build file)
- Gradle 8.11.1, AGP 8.9.2, Kotlin 2.0.21, Compose BOM 2024.12.01
- minSdk 26, targetSdk 35

## Conventions
- Hardcoded strings **in Spanish** allowed (no `strings.xml` at this stage)
- Colors: always use constants from `ui/theme/Color.kt` — never inline `Color(0xFF...)`
- Screens: `@Composable fun NombreScreen()` in own file under `ui/<modulo>/`
- ViewModel: one per screen, same package
- Custom icons in `SharedComponents.kt` via `IconoPersonalizado(name=...)`

## Git workflow
- Base: `base/setup` → feature branches: `feature/<name>-<module>`
- No direct merges to `base/setup`
- Test credentials: `admin@citygrid.com` / `citygrid123`

## Gotchas
- README is partially stale: actual package is `com.example.citygrid` (not `com.citygrid.app`), JVM target is 11 (not 17)
- No CI, no lint/typecheck tooling set up
- `secrets.xml` pattern in README is not used — creds live in `Constants.kt`
- `MqttManager.connect()` / `disconnect()` / `publish()` are stubs — MQTT topics must be consumed via `residuosFlow`, `aguaFlow`, `alumbradoFlow`, `alertasFlow`
- Broker: `tcp://broker.hivemq.com:1883`
