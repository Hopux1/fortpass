pluginManagement {
    repositories {
        gradlePluginPortal()
        google() // Repositorio de Google
        mavenCentral() // Repositorio Maven Central
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google() // Repositorio de Google
        mavenCentral() // Repositorio Maven Central
    }
}

// Asegúrate de incluir el módulo 'app'
rootProject.name = "AppGestion"
include(":app")
