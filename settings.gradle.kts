pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
        maven("https://maven.quiltmc.org/repository/release")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.6"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    shared {
        versions("1.20.1", "1.20.4", "1.20.6", "1.21.1", "1.21.3", "1.21.6", "1.21.9")
    }
    create(rootProject)
}

rootProject.name = "Zoomify"

