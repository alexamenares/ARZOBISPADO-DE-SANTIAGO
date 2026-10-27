/*
 * settings.gradle.kts define el proyecto y de dónde descarga Gradle sus herramientas.
 * pluginManagement configura repositorios de plugins; dependencyResolutionManagement,
 * los de bibliotecas. include(":app") registra el módulo Android de esta aplicación.
 * El plugin Foojay ayuda a resolver las herramientas Java solicitadas por Gradle.
 * Este archivo se lee durante la preparación de la compilación.
 */
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    /*
     * Exige que los repositorios se definan en este archivo para evitar configuraciones dispersas.
     */
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

/*
 * Nombre del proyecto en Android Studio. El nombre visible de la app está en strings.xml.
 */
rootProject.name = "RedPatrimonioReligioso"
include(":app")
 
