/*
 * build.gradle.kts de la raíz declara plugins compartidos del proyecto.
 * apply false pone sus versiones a disposición sin aplicarlos al proyecto raíz.
 * El módulo app los activa en su propio build.gradle.kts.
 * Para explicar dependencias de pantallas o Room se revisa el archivo del módulo app.
 */
// Configuración de plugins común a los módulos del proyecto.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
