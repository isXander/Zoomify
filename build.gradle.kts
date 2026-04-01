plugins {
    `java-library`

    val kotlinVersion = "2.3.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("dev.isxander.modstitch.base") version "0.8.4"

    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
    `maven-publish`
    signing
    id("dev.isxander.secrets")
    id("com.gradleup.nmcp")
    id("org.ajoberstar.grgit") version "5.3.2"
}

modstitch {
    minecraftVersion = property("mcVersion").toString()

    metadata {
        modId = providers.gradleProperty("modId")
        modName = providers.gradleProperty("modName")
        modVersion = providers.gradleProperty("modVersion").map { "$it+${stonecutter.current.project}" }
        modDescription = providers.gradleProperty("modDescription")
        modGroup = "dev.isxander"
        modLicense = "LGPL-3.0-or-later"
        modAuthor = "isXander"

        replacementProperties.put(
            "minecraft_dependency",
            findProperty("fmj.mcDep")?.toString()!!
        )
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

    fun String.propDefined() = project.findProperty(this)?.toString()?.isNotBlank() ?: false

    constants {
        put("mod-menu", "deps.modMenu".propDefined())
        put("controlify", "deps.controlify".propDefined())
    }

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
}

dependencies {
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

}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.javadoc {
    isFailOnError = false
}

kotlin {
    jvmToolchain {
        languageVersion = modstitch.javaVersion.map { JavaLanguageVersion.of(it) }
    }
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
            from(components["java"])

            groupId = "dev.isxander"
            artifactId = "zoomify"
            version = modstitch.metadata.modVersion.get()

            pom {
                name = modstitch.metadata.modName
                description = modstitch.metadata.modDescription
                url = "https://www.isxander.dev/projects/zoomify"
                licenses {
                    license {
                        name = "LGPL-3.0-or-later"
                        url = "https://www.gnu.org/licenses/lgpl-3.0.en.html"
                    }
                }
                developers {
                    developer {
                        id = "isXander"
                        name = "Xander"
                        email = "business@isxander.dev"
                    }
                }
                scm {
                    url = "https://github.com/isXander/Zoomify"
                    connection = "scm:git:git//github.com/isXander/Zoomify.git"
                    developerConnection = "scm:git:ssh://git@github.com/isXander/Zoomify.git"
                }
            }
        }
    }
}
val signingKeyProvider = secrets.gradleProperty("signing.secretKey")
val signingPasswordProvider = secrets.gradleProperty("signing.password")
signing {
    sign(publishing.publications["mod"])
}
// not configuration cache friendly, but neither is the whole of signing plugin
// this plugin does not support lazy configuration of signing keys
gradle.taskGraph.whenReady {
    val willSign = allTasks.any { it.name.startsWith("sign") }
    if (willSign) {
        signing {
            val signingKey = signingKeyProvider.orNull
            val signingPassword = signingPasswordProvider.orNull

            isRequired = signingKey != null && signingPassword != null
            if (isRequired) {
                useInMemoryPgpKeys(signingKey, signingPassword)
            } else {
                logger.error("Signing keys not found; skipping signing!")
            }
        }
    }
}

fun <T> optionalProp(property: String, block: (String) -> T?) {
    findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)
}

fun isPropDefined(property: String): Boolean {
    return findProperty(property)?.toString()?.isNotBlank() ?: false
}
