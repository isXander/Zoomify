plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)

    alias(libs.plugins.loom)
    alias(libs.plugins.loom.vineflower)

    id("com.modrinth.minotaur") version "2.7.+"
    id("me.hypherionmc.cursegradle") version "2.+"
    id("com.github.breadmoirai.github-release") version "2.4.+"
    `maven-publish`

    id("io.github.p03w.machete") version "2.0.+"
}

group = "dev.isxander"
version = "2.12.0"

repositories {
    mavenCentral()
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.isxander.dev/snapshots")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases")
    maven("https://jitpack.io")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.quiltmc.org/repository/release/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        mappings("org.quiltmc:quilt-mappings:${libs.versions.minecraft.get()}+build.${libs.versions.quilt.mappings.get()}:intermediary-v2")
        officialMojangMappings()
    })
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.language.kotlin)

    implementation(libs.ktoml.core)
    include(libs.ktoml.core)

    modApi(libs.yet.another.config.lib)

    implementation(libs.bundles.settxi)
    include(libs.bundles.settxi)

    modImplementation(libs.mod.menu)

    modImplementation(libs.controlify)

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
        gameVersions.set(listOf("1.20.2"))
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
            addGameVersion("1.20.2")
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
    targetCommitish("1.20")
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

tasks["githubRelease"].dependsOn("optimizeOutputsOfRemapJar")
tasks["modrinth"].dependsOn("optimizeOutputsOfRemapJar")
tasks["generateMetadataFileForZoomifyPublication"].dependsOn("optimizeOutputsOfRemapJar")
