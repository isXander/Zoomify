plugins {
    val kotlinVersion: String by System.getProperties()

    java
    kotlin("jvm") version kotlinVersion
    id("fabric-loom") version "0.11.+"
}

group = "dev.isxander"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.sk1er.club/repository/maven-public")
    maven("https://maven.terraformersmc.com/releases")
    maven("https://jitpack.io")
}

fun DependencyHandlerScope.includeModImplementation(dependency: Any) {
    include(dependency)
    modImplementation(dependency)
}

fun DependencyHandlerScope.includeImplementation(dependency: Any) {
    include(dependency)
    implementation(dependency)
}

dependencies {
    val kotlinVersion: String by System.getProperties()
    val minecraftVersion: String by project
    val yarnVersion: String by project
    val loaderVersion: String by project
    val fabricVersion: String by project
    val fabricKotlinVersion: String by project

    implementation(kotlin("stdlib-jdk8", kotlinVersion))

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnVersion:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion+kotlin.$kotlinVersion")

    includeModImplementation("gg.essential:vigilance-1.18-fabric:+")
    modImplementation("com.terraformersmc:modmenu:3.0.+")

    includeImplementation("com.github.llamalad7:mixinextras:0.0.+")
    annotationProcessor("com.github.llamalad7:mixinextras:0.0.+")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "version" to project.version
                )
            )
        }
    }
}
