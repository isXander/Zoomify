plugins {
    `java-library`

    val kotlinVersion = "2.3.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("dev.isxander.modstitch.base") version "0.8.4"

    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
    `maven-publish`
    signing
    id("dev.isxander.secrets") version "0.1.0"
    id("org.ajoberstar.grgit") version "5.3.2"
}

modstitch {
    minecraftVersion = property("mcVersion").toString()

    parchment {
        mappingsVersion = providers.gradleProperty("parchment.version")
        minecraftVersion = providers.gradleProperty("parchment.minecraft")
    }

    metadata {
        modId = providers.gradleProperty("modId")
        modName = providers.gradleProperty("modName")
        modVersion = providers.gradleProperty("modVersion").map { "$it+${stonecutter.current.project}" }
        modDescription = providers.gradleProperty("modDescription")
        modGroup = "dev.isxander"
        modLicense = "LGPL-3.0-or-later"
        modAuthor = "isXander"
    }

    loom {
        fabricLoaderVersion = providers.gradleProperty("deps.fabricLoader")

        configureLoom {
            runConfigs.all {
                runDir("../../run")
            }
        }
    }

    mixin {
        addMixinsToModManifest = true
        configs.register("zoomify")
    }
}


val isAlpha = "alpha" in modstitch.metadata.modVersion.get()
val isBeta = "beta" in modstitch.metadata.modVersion.get()

stonecutter {
    swaps.put(
        "fov-precision",
        if (stonecutter.eval(stonecutter.current.version, ">=1.21.2"))
            "float" else "double"
    )

}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://maven.terraformersmc.com") }
        filter { includeGroup("com.terraformersmc") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.quiltmc.org/repository/release") }
        filter { includeGroupAndSubgroups("org.quiltmc") }
    }
    maven("https://maven.isxander.dev/releases")
    exclusiveContent {
        forRepository { maven("https://maven.parchmentmc.org") }
        filter { includeGroup("org.parchmentmc.data") }
    }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") }
        filter { includeGroup("maven.modrinth") }
    }
    exclusiveContent {
        forRepository { maven("https://cursemaven.com") }
        filter { includeGroup("curse.maven") }
    }
}

dependencies {
    fun Dependency?.jij() = this?.also(::modstitchJiJ)

    modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabricApi")}")
    modstitchModImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.flk")}")
    modstitchModApi("dev.isxander:yet-another-config-lib:${property("deps.yacl")}")

    // mod menu compat
    optionalProp("deps.modMenu") {
        modstitchModCompileOnly("com.terraformersmc:modmenu:$it")
    }

    optionalProp("deps.controlify") {
        modstitchModCompileOnly("dev.isxander:controlify:$it")
    }

    modstitchImplementation("com.akuleshov7:ktoml-core-jvm:${property("deps.ktoml")}").jij()
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.javadoc {
    isFailOnError = false
}

kotlin {
    //jvmToolchain(modstitch.javaVersion)
}

tasks.register("releaseMod") {
    group = "mod"

    dependsOn("publishMods")
    dependsOn("publish")
}

publishMods {
    displayName = modstitch.metadata.modVersion.map { "Zoomify $it" }
    file = modstitch.finalJarTask.flatMap { it.archiveFile }
    changelog = providers.provider {
        rootProject.file("changelog.md")
            .takeIf { it.exists() }
            ?.readText()
            ?: "No changelog provided."
    }
    type.set(when {
        isAlpha -> ALPHA
        isBeta -> BETA
        else -> STABLE
    })
    modLoaders.add("fabric")

    fun versionList(prop: String) = findProperty(prop)?.toString()
        ?.split(',')
        ?.map { it.trim() }
        ?: emptyList()

    // modrinth and curseforge use different formats for snapshots. this can be expressed globally
    val stableMCVersions = versionList("pub.stableMC")

    modrinth {
        accessToken = secrets.gradleProperty("modrinth.accessToken")
        projectId = providers.gradleProperty("pub.modrinthId")

        minecraftVersions.addAll(stableMCVersions)
        minecraftVersions.addAll(versionList("pub.modrinthMC"))

        requires { slug.set("fabric-api") }
        requires { slug.set("yacl") }
        requires { slug.set("fabric-language-kotlin") }
        optional { slug.set("modmenu") }
    }

    curseforge {
        accessToken = secrets.gradleProperty("curseforge.accessToken")
        projectId = providers.gradleProperty("pub.curseforgeId")
        projectSlug = providers.gradleProperty("pub.curseforgeSlug")

        minecraftVersions.addAll(stableMCVersions)
        minecraftVersions.addAll(versionList("pub.curseMC"))

        requires { slug.set("fabric-api") }
        requires { slug.set("yacl") }
        requires { slug.set("fabric-language-kotlin") }
        optional { slug.set("modmenu") }
    }

    github {
        accessToken = secrets.gradleProperty("github.accessToken")
        repository = providers.gradleProperty("githubProject")
        commitish = grgit.branch.current().name
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
        } else {
            println("Xander Maven credentials not satisfied.")
        }
    }
}

fun <T> optionalProp(property: String, block: (String) -> T?) {
    findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)
}

fun isPropDefined(property: String): Boolean {
    return findProperty(property)?.toString()?.isNotBlank() ?: false
}
