plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)

    alias(libs.plugins.loom)
    alias(libs.plugins.loom.quiltflower)

    id("com.modrinth.minotaur") version "2.4.+"
    id("me.hypherionmc.cursegradle") version "2.+"
    id("com.github.breadmoirai.github-release") version "2.4.+"
    `maven-publish`

    id("io.github.p03w.machete") version "2.0.+"
}

group = "dev.isxander"
version = "2.9.2"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.isxander.dev/snapshots")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases")
    maven("https://jitpack.io")
}

dependencies {
    minecraft(libs.minecraft)
    mappings("net.fabricmc:yarn:${libs.versions.minecraft.get()}+build.+:v2")
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.language.kotlin)

    implementation(libs.ktoml.core)
    include(libs.ktoml.core)

    modApi(libs.yet.another.config.lib)

    implementation(libs.settxi.core)
    include(libs.settxi.core)
    implementation(libs.settxi.serialization.kotlinx)
    include(libs.settxi.serialization.kotlinx)
    include(libs.settxi.gui)
    modImplementation(libs.settxi.gui.yacl) {
        artifact { classifier = "fabric-1.19.3" }
    }
    include(libs.settxi.gui.yacl) {
        artifact { classifier = "fabric-1.19.3" }
    }

    modImplementation(libs.mod.menu)

    libs.mixin.extras.let {
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
        dependsOn("publish")
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
        gameVersions.set(listOf("1.19.3"))
        loaders.set(listOf("fabric", "quilt"))
        changelog.set(changelogText)
        syncBodyFrom.set(file("README.md").readText())
        dependencies {
            required.project("fabric-api")
            required.project("fabric-language-kotlin")
            required.project("yacl")
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
            addGameVersion("1.19.3")
            addGameVersion("Fabric")
            addGameVersion("Quilt")
            addGameVersion("Java 17")

            relations(closureOf<me.hypherionmc.cursegradle.CurseRelation> {
                requiredDependency("fabric-api")
                requiredDependency("fabric-language-kotlin")
                requiredDependency("yacl")
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
    targetCommitish("1.19.3")
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
        if (hasProperty("XANDER_MAVEN_USER") && hasProperty("XANDER_MAVEN_PASS")) {
            maven("https://maven.isxander.dev/releases") {
                credentials {
                    username = findProperty("XANDER_MAVEN_USER")?.toString()
                    password = findProperty("XANDER_MAVEN_PASS")?.toString()
                }
            }
        } else println("Cannot publish to https://maven.isxander.dev")
    }
}
