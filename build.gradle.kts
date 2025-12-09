plugins {
    `java-library`

    val kotlinVersion = "2.2.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("fabric-loom") version "1.14-SNAPSHOT"

    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
    `maven-publish`

    id("org.ajoberstar.grgit") version "5.3.2"
}

val mcVersion = property("mcVersion")!!.toString()
val mcSemverVersion = stonecutter.current.version
val mcDep = property("fmj.mcDep").toString()

group = "dev.isxander"
val versionWithoutMC = "2.14.6"
version = "$versionWithoutMC+${stonecutter.current.project}"

val isAlpha = "alpha" in version.toString()
val isBeta = "beta" in version.toString()

base {
    archivesName.set(property("modName").toString())
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        runDir("../../run")
    }

    mixin {
        useLegacyMixinAp.set(false)
    }
}

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
    exclusiveContent {
        forRepository { mavenLocal() }
        filter {
            includeVersionByRegex("net\\.fabricmc\\.fabric-api", "fabric.*", ".*local")
            includeVersion("dev.isxander", "yet-another-config-lib", "3.7.1+1.21.9-fabric")
        }
    }
}

dependencies {
    fun Dependency?.jij() = this?.also(::include)

    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        optionalProp("deps.parchment") {
            parchment("org.parchmentmc.data:parchment-$it@zip")
        }

        officialMojangMappings()
    })
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabricLoader")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabricApi")}")

    modImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.flk")}")

    modApi("dev.isxander:yet-another-config-lib:${property("deps.yacl")}") {
        // was including old fapi version that broke things at runtime
        exclude(group = "net.fabricmc.fabric-api")
    }.jij()

    // mod menu compat
    optionalProp("deps.modMenu") {
        modCompileOnly("com.terraformersmc:modmenu:$it") {
            exclude(group = "net.fabricmc.fabric-api")
        }
    }

    optionalProp("deps.controlify") {
        modCompileOnly("dev.isxander:controlify:$it")  {
            exclude(group = "net.fabricmc.fabric-api")
        }
    }

    modImplementation(include("com.akuleshov7:ktoml-core-jvm:${property("deps.ktoml")}")!!)
}

tasks {
    processResources {
        val modId: String by project
        val modName: String by project
        val modDescription: String by project
        val githubProject: String by project

        val props = mapOf(
            "id" to modId,
            "group" to project.group,
            "name" to modName,
            "description" to modDescription,
            "version" to project.version,
            "github" to githubProject,
            "mc" to mcDep
        )

        props.forEach(inputs::property)

        filesMatching("fabric.mod.json") {
            expand(props)
        }

        eachFile {
            // don't include photoshop files for the textures for development
            if (name.endsWith(".psd")) {
                exclude()
            }
        }
    }

    register("releaseMod") {
        group = "mod"

        dependsOn("publishMods")
        dependsOn("publish")
    }
}

val javaMajorVersion = property("java.version").toString().toInt()
java {
    withSourcesJar()

    javaMajorVersion
        .let { JavaVersion.values()[it - 1] }
        .let {
            sourceCompatibility = it
            targetCompatibility = it
        }
}

tasks.withType<JavaCompile> {
    options.release = javaMajorVersion
}
kotlin {
    jvmToolchain(javaMajorVersion)
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

    fun versionList(prop: String) = findProperty(prop)?.toString()
        ?.split(',')
        ?.map { it.trim() }
        ?: emptyList()

    // modrinth and curseforge use different formats for snapshots. this can be expressed globally
    val stableMCVersions = versionList("pub.stableMC")

    val modrinthId: String by project
    if (modrinthId.isNotBlank() && hasProperty("modrinth.token")) {
        modrinth {
            projectId.set(modrinthId)
            accessToken.set(findProperty("modrinth.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)
            minecraftVersions.addAll(versionList("pub.modrinthMC"))

            requires { slug.set("fabric-api") }
            requires { slug.set("yacl") }
            requires { slug.set("fabric-language-kotlin") }
            optional { slug.set("modmenu") }
        }
    }

    val curseforgeId: String by project
    if (curseforgeId.isNotBlank() && hasProperty("curseforge.token")) {
        curseforge {
            projectId.set(curseforgeId)
            accessToken.set(findProperty("curseforge.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)
            minecraftVersions.addAll(versionList("pub.curseMC"))

            requires { slug.set("fabric-api") }
            requires { slug.set("yacl") }
            requires { slug.set("fabric-language-kotlin") }
            optional { slug.set("modmenu") }
        }
    }

    val githubProject: String by project
    if (githubProject.isNotBlank() && hasProperty("github.token")) {
        github {
            repository.set(githubProject)
            accessToken.set(findProperty("github.token")?.toString())
            commitish.set(grgit.branch.current().name)
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
