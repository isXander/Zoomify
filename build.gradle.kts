plugins {
    java
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"

    id("fabric-loom") version "1.6.+"

    id("me.modmuss50.mod-publish-plugin") version "0.5.+"
    `maven-publish`

    id("io.github.p03w.machete") version "2.+"
    id("org.ajoberstar.grgit") version "5.0.+"
}

val mcVersion = stonecutter.current.version
val mcDep = property("fmj.mcDep").toString()

group = "dev.isxander"
val versionWithoutMC = "2.14.0"
version = "$versionWithoutMC+${stonecutter.current.project}"
val isAlpha = "alpha" in version.toString()
val isBeta = "beta" in version.toString()

base {
    archivesName.set(property("modName").toString())
}

stonecutter.expression {
    when (it) {
        "controlify" -> isPropDefined("deps.controlify")
        "mod-menu" -> isPropDefined("deps.modMenu")
        else -> null
    }
}

loom {
    if (stonecutter.current.isActive) {
        runConfigs.all {
            ideConfigGenerated(true)
            runDir("../../run")
        }
    }

    mixin {
        useLegacyMixinAp.set(false)
    }
}

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.isxander.dev/snapshots")
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://api.modrinth.com/maven") {
        content {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")

    mappings(loom.layered {
        optionalProp("deps.quiltMappings") {
            mappings("org.quiltmc:quilt-mappings:$mcVersion+build.$it:intermediary-v2")
        }
        officialMojangMappings()
    })
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabricLoader")}")

    val fapiVersion = property("deps.fabricApi").toString()
    listOf(
        "fabric-resource-loader-v0",
        "fabric-lifecycle-events-v1",
        "fabric-key-binding-api-v1",
        "fabric-command-api-v2",
    ).forEach {
        modImplementation(fabricApi.module(it, fapiVersion))
    }
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:$fapiVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.flk")}")

    modApi("dev.isxander.yacl:yet-another-config-lib-fabric:${property("deps.yacl")}") {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
    }

    optionalProp("deps.modMenu") {
        modImplementation("com.terraformersmc:modmenu:$it")
    }

    optionalProp("deps.controlify") {
        modImplementation("dev.isxander:controlify:$it")
    }

    listOf(
        "settxi-core",
        "settxi-kotlinx-serialization"
    ).forEach {
        implementation("dev.isxander.settxi:$it:${property("deps.settxi")}")
        include("dev.isxander.settxi:$it:${property("deps.settxi")}")
    }

    implementation("com.akuleshov7:ktoml-core-jvm:${property("deps.ktoml")}")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    processResources {
        val props = mutableMapOf(
            "id" to findProperty("modId"),
            "group" to project.group,
            "name" to findProperty("modName"),
            "description" to findProperty("modDescription"),
            "version" to project.version,
            "github" to findProperty("githubProject"),
            "mc" to mcDep
        )
        optionalProp("fmj.yaclDep") {
            props["yacl"] = it
        }

        props.forEach(inputs::property)

        filesMatching("fabric.mod.json") {
            expand(props)
        }
    }

    register("releaseMod") {
        group = "mod"

        dependsOn("publishMods")
        dependsOn("publish")
    }
}

machete {
    json.enabled.set(false)
}

publishMods {
    displayName.set("Zoomify $versionWithoutMC for MC $mcVersion")
    file.set(tasks.remapJar.get().archiveFile)
    changelog.set(
        rootProject.file("changelogs/${versionWithoutMC}.md")
            .takeIf { it.exists() }
            ?.readText()
            ?: "No changelog provided."
    )
    type.set(when {
        isAlpha -> ALPHA
        isBeta -> BETA
        else -> STABLE
    })
    modLoaders.add("fabric")

    // modrinth and curseforge use different formats for snapshots. this can be expressed globally
    val stableMCVersions = listOf(stonecutter.current.project)

    val modrinthId: String by project
    if (modrinthId.isNotBlank() && hasProperty("modrinth.token")) {
        modrinth {
            projectId.set(modrinthId)
            accessToken.set(findProperty("modrinth.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)

            requires { slug.set("fabric-api") }
            requires { slug.set("yacl") }
            optional { slug.set("modmenu") }
            optional { slug.set("controlify") }
        }

        tasks.getByName("publishModrinth") {
            dependsOn("optimizeOutputsOfRemapJar")
        }
    }

    val curseforgeId: String by project
    if (curseforgeId.isNotBlank() && hasProperty("curseforge.token")) {
        curseforge {
            projectId.set(curseforgeId)
            accessToken.set(findProperty("curseforge.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)

            requires { slug.set("fabric-api") }
            requires { slug.set("yacl") }
            optional { slug.set("modmenu") }
            optional { slug.set("controlify") }
        }

        tasks.getByName("publishCurseforge") {
            dependsOn("optimizeOutputsOfRemapJar")
        }
    }

    val githubProject: String by project
    if (githubProject.isNotBlank() && hasProperty("github.token")) {
        github {
            repository.set(githubProject)
            accessToken.set(findProperty("github.token")?.toString())
            commitish.set(grgit.branch.current().name)
        }

        tasks.getByName("publishGithub") {
            dependsOn("optimizeOutputsOfRemapJar")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mod") {
            groupId = "dev.isxander"
            artifactId = "zoomify"

            from(components["java"])
        }
    }

    repositories {
        val username = "XANDER_MAVEN_USER".let { System.getenv(it) ?: findProperty(it) }?.toString()
        val password = "XANDER_MAVEN_PASS".let { System.getenv(it) ?: findProperty(it) }?.toString()
        if (username != null && password != null) {
            maven(url = "https://maven.isxander.dev/releases") {
                name = "XanderReleases"
                credentials {
                    this.username = username
                    this.password = password
                }
            }
            tasks.getByName("publishModPublicationToXanderReleasesRepository") {
                dependsOn("optimizeOutputsOfRemapJar")
            }
        } else {
            println("Xander Maven credentials not satisfied.")
        }
    }
}

tasks.getByName("generateMetadataFileForModPublication") {
    dependsOn("optimizeOutputsOfRemapJar")
}

fun <T> optionalProp(property: String, block: (String) -> T?) {
    findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)
}

fun isPropDefined(property: String): Boolean {
    return property(property)?.toString()?.isNotBlank() ?: false
}
