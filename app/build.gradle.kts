plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.citygrid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.citygrid"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose Extras
    implementation("androidx.compose.material:material-icons-extended")

    // Lifecycle y ViewModel para Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Navegación
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // MQTT Paho
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")

    // Gráfico semicírculo (MPAndroidChart)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // DataStore (Preferencias)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

   // BOM de Supabase para controlar las versiones de todos sus módulos
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.2"))

    // Módulos principales que seguro usarás
    implementation("io.github.jan-tennert.supabase:postgrest-kt") // Base de datos
    implementation("io.github.jan-tennert.supabase:auth-kt")    // Autenticación
    implementation("io.github.jan-tennert.supabase:realtime-kt")  // Realtime
    // implementation("io.github.jan-tennert.supabase:storage-kt") // Descomenta si usas Storage

    // Cliente HTTP necesario para que Supabase funcione en Android
    implementation("io.ktor:ktor-client-okhttp:3.0.0")

    // Serialización (Para convertir JSON a objetos de Kotlin)
    implementation(libs.kotlinx.serialization.json)
}