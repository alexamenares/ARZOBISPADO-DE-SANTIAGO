/*
 * build.gradle.kts del módulo app configura cómo se compila la aplicación Android.
 * Aquí se activan plugins, versiones de SDK, Compose y las bibliotecas utilizadas.
 * Los alias libs se resuelven en gradle/libs.versions.toml.
 * Los cambios en este archivo requieren sincronizar Gradle en Android Studio.
 * Este archivo no se ejecuta como una pantalla ni contiene credenciales del backend.
 */
/*
 * Android application construye la app, Kotlin Compose prepara el compilador Compose
 * y KSP procesa las anotaciones de Room para generar su implementación.
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    /*
     * namespace determina el paquete de clases generadas como R. applicationId identifica
     * la aplicación instalada; no es el nombre visible que se muestra bajo el icono.
     */
    namespace = "cl.duoc.redpatrimonio"
    /*
     * Se compila con las APIs del SDK 37. minSdk=36 es el Android mínimo admitido.
     * targetSdk=37 declara el nivel de comportamiento de Android para el que se prepara la app.
     */
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.redpatrimonio"
        minSdk = 36
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        /*
         * Configuración de optimización para la variante release. Se conservan los valores
         * originales; el escáner se probó manualmente en la variante de desarrollo.
         */
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    /*
     * VERSION_11 se refiere a compatibilidad del código Java compilado. No es la versión
     * del JDK que ejecuta Gradle; ese proceso tiene su configuración de JVM aparte.
     */
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

/*
 * implementation incorpora bibliotecas para la app. testImplementation corresponde
 * a pruebas locales, androidTestImplementation a pruebas en Android y debugImplementation
 * a ayudas que solo se incluyen en la variante debug.
 */
dependencies {
    /*
     * El BOM coordina las versiones de las bibliotecas Compose sin repetirlas en cada alias.
     */
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    /*
     * Navigation Compose aporta NavHost, composable y rememberNavController.
     */
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Room guarda los favoritos de cada cuenta en una base de datos local.
    implementation(libs.androidx.room3.runtime)
    // Controlador SQLite usado por Room para crear la base de datos local.
    implementation(libs.androidx.sqlite.bundled)
    /*
     * Google Code Scanner abre la interfaz de lectura QR de Google Play Services.
     * No es el lector directo anterior de ML Kit ni necesita CameraX en nuestro código.
     */
    implementation(libs.google.code.scanner)

// KSP genera automáticamente el código interno que necesita Room.
    ksp(libs.androidx.room3.compiler)

    /*
     * Permite obtener ViewModel desde funciones Compose con viewModel().
     */
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
}

