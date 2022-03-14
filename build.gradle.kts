plugins {
    val kotlinVersion: String by System.getProperties()

    java
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("fabric-loom") version "0.11.+"
}

group = "dev.isxander"
version = "1.4.0"

repositories {
    mavenCentral()
    maven("https://repo.sk1er.club/repository/maven-public")
    maven("https://maven.terraformersmc.com/releases")
    maven("https://jitpack.io")
    maven("https://maven.shedaniel.me/")
    maven("https://repo.woverflow.cc/")
}

fun DependencyHandlerScope.includeImplementation(dependency: String, action: Action<ExternalModuleDependency> = Action {}): Dependency? {
    return implementation(include(dependency, action))
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

    modApi("me.shedaniel.cloth:cloth-config-fabric:6.+") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    include(implementation("dev.isxander:settxi:2.1.1")!!)
    include(modImplementation("dev.isxander:settxi-cloth-impl:1.0.1:fabric-1.18.2")!!)

    include(implementation("org.bundleproject:libversion:0.0.3")!!)

    modImplementation("com.terraformersmc:modmenu:3.+")

    include(implementation("com.github.llamalad7:mixinextras:0.0.+")!!)
    annotationProcessor("com.github.llamalad7:mixinextras:0.0.+")
}

tasks {
    withType<JavaCompile> {
        options.release.set(17)
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

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
