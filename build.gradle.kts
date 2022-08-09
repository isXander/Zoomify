plugins {
    val kotlinVersion: String by System.getProperties()

    java
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("fabric-loom") version "0.12.+"
    id("io.github.juuxel.loom-quiltflower") version "1.7.+"

    id("com.modrinth.minotaur") version "2.4.+"
    id("me.hypherionmc.cursegradle") version "2.+"
    id("com.github.breadmoirai.github-release") version "2.4.+"
    `maven-publish`

    id("io.github.p03w.machete") version "1.1.+"
}

group = "dev.isxander"
version = "2.1.0"

repositories {
    mavenCentral()
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases")
    maven("https://jitpack.io")
}

val minecraftVersion: String by project

dependencies {
    val kotlinVersion: String by System.getProperties()
    val loaderVersion: String by project
    val fabricVersion: String by project
    val fabricKotlinVersion: String by project
    val settxiVersion: String by project

    implementation(kotlin("stdlib-jdk8", kotlinVersion))

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$minecraftVersion+build.+:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion+kotlin.$kotlinVersion")

    modApi("me.shedaniel.cloth:cloth-config-fabric:8.0.+") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    include(implementation("dev.isxander.settxi:settxi-core:$settxiVersion")!!)
    include(implementation("dev.isxander.settxi:settxi-kotlinx-serialization:$settxiVersion")!!)
    include(modImplementation("dev.isxander.settxi:settxi-gui-cloth-config:$settxiVersion:fabric-1.19.2")!!)

    modImplementation("com.terraformersmc:modmenu:4.0.6")

    "com.github.llamalad7:mixinextras:0.0.+".let {
        implementation(it)
        annotationProcessor(it)
        include(it)
    }
}

java {
    withSourcesJar()
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
                "version" to project.version
            )
        }
    }

    register("releaseMod") {
        group = "mod"

        dependsOn("modrinth")
        dependsOn("modrinthSyncBody")
        dependsOn("curseforge")
        dependsOn("githubRelease")
    }
}

val changelogText = file("changelogs/${project.version}.md").takeIf { it.exists() }?.readText() ?: "No changelog provided"

val modrinthId: String by project
if (modrinthId.isNotEmpty()) {
    modrinth {
        token.set(findProperty("modrinth.token")?.toString())
        projectId.set(modrinthId)
        versionNumber.set("${project.version}")
        versionType.set("release")
        uploadFile.set(tasks["remapJar"])
        gameVersions.set(listOf("1.19", "1.19.1", "1.19.2"))
        loaders.set(listOf("fabric", "quilt"))
        changelog.set(changelogText)
        syncBodyFrom.set(file("README.md").readText())
        dependencies {
            required.project("fabric-api")
            required.project("fabric-language-kotlin")
            required.project("cloth-config")
            optional.project("modmenu")
        }
    }
}

val curseforgeId: String by project
if (hasProperty("curseforge.token") && curseforgeId.isNotEmpty()) {
    curseforge {
        apiKey = findProperty("curseforge.token")
        project(closureOf<me.hypherionmc.cursegradle.CurseProject> {
            mainArtifact(tasks["remapJar"], closureOf<me.hypherionmc.cursegradle.CurseArtifact> {
                displayName = "${project.version}"
            })

            id = curseforgeId
            releaseType = "release"
            addGameVersion("1.19")
            addGameVersion("1.19.1")
            addGameVersion("1.19.2")
            addGameVersion("Fabric")
            addGameVersion("Quilt")
            addGameVersion("Java 17")

            relations(closureOf<me.hypherionmc.cursegradle.CurseRelation> {
                requiredDependency("fabric-api")
                requiredDependency("fabric-language-kotlin")
                requiredDependency("cloth-config")
                optionalDependency("modmenu")
            })

            changelog = changelogText
            changelogType = "markdown"
        })

        options(closureOf<me.hypherionmc.cursegradle.Options> {
            forgeGradleIntegration = false
        })
    }
}

githubRelease {
    token(findProperty("github.token")?.toString())

    val githubProject: String by project
    val split = githubProject.split("/")
    owner(split[0])
    repo(split[1])
    tagName("${project.version}")
    targetCommitish("1.19")
    body(changelogText)
    releaseAssets(tasks["remapJar"].outputs.files)
}

publishing {
    publications {
        register<MavenPublication>("zoomify") {
            groupId = "dev.isxander"
            artifactId = "zoomify"

            from(components["java"])
        }
    }

    repositories {
        if (hasProperty("xander-repo.username") && hasProperty("xander-repo.password")) {
            maven("https://maven.isxander.dev/releases") {
                credentials {
                    username = findProperty("xander-repo.username")?.toString()
                    password = findProperty("xander-repo.password")?.toString()
                }
            }
        } else println("Cannot publish to https://maven.isxander.dev")
    }
}
