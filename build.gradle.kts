import org.gradle.api.attributes.Attribute

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.neoforged.gradle.userdev) apply false
    alias(libs.plugins.modstitch.multiloader)
    alias(libs.plugins.modstitch.manifests)
    alias(libs.plugins.modstitch.modrepos)
    alias(libs.plugins.mod.publish.plugin)
    `maven-publish`
}

group = "dev.isxander"

val modVersion = providers.gradleProperty("mod.version").get()
val minecraftVersion = property("dep.minecraft")!!.toString()
version = "$modVersion+mc$minecraftVersion"

base.archivesName = "zoomify"

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

repositories {
    mavenCentral()
    isxander()
    modrinthApi.exclusive()
    exclusiveContent {
        forRepository { maven("https://maven.quiltmc.org/repository/release") }
        filter { includeGroupAndSubgroups("org.quiltmc") }
    }
    exclusiveContent {
        forRepository { maven(url = "https://repo.nyon.dev/releases") }
        filter {
            includeGroupAndSubgroups("dev.nyon")
        }
    }
}

val fabricModDependency = configurations.dependencyScope("fabricModDependency")
configurations.fabricCompileOnly { extendsFrom(fabricModDependency) }
configurations.fabricLocalRuntime { extendsFrom(fabricModDependency) }

val neoforgeModDependency = configurations.dependencyScope("neoforgeModDependency")
configurations.neoforgeCompileOnly { extendsFrom(neoforgeModDependency) }
configurations.neoforgeLocalRuntime { extendsFrom(neoforgeModDependency) }

val fabricApiBom = dependencies.platform("net.fabricmc.fabric-api:fabric-api-bom:${property("dep.fapi")}")

// Tests exercise the common source set and inherit its dependencies.
sourceSets.test {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName).forEach { classpathName ->
        configurations.named(classpathName) {
            attributes.attribute(Attribute.of("io.github.mcgradleconventions.loader", String::class.java), "common")
        }
    }
}

// NeoGradle's discovery configurations inherit dependencies but omit
// classpath attributes, including Modstitch's loader attribute.
sourceSets.configureEach {
    val sourceSetName = name
    mapOf(
        "Compile" to compileClasspathConfigurationName,
        "Runtime" to runtimeClasspathConfigurationName,
    ).forEach { (kind, classpathName) ->
        val classpath = configurations.named(classpathName)
        configurations.matching { it.name == "${sourceSetName}${kind}DependencyResolve" }.configureEach {
            val classpathAttributes = classpath.get().attributes
            fun <T : Any> copyAttribute(key: Attribute<T>) {
                attributes.attribute(key, classpathAttributes.getAttribute(key)!!)
            }
            classpathAttributes.keySet().forEach { copyAttribute(it) }
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    fabricLoader(libs.fabric.loader)

    ifPresent("dep.neoforge") {
        neoforgeImplementation("net.neoforged:neoforge:${property("dep.neoforge")}")
    }

    implementation(fabricApiBom)
    fabricImplementation(fabricApiBom)
    fabricImplementation("net.fabricmc.fabric-api:fabric-resource-loader-v1")
    fabricImplementation("net.fabricmc.fabric-api:fabric-key-mapping-api-v1")
    fabricImplementation("net.fabricmc.fabric-api:fabric-command-api-v2")
    fabricImplementation("net.fabricmc.fabric-api:fabric-lifecycle-events-v1")
    fabricLocalRuntime("net.fabricmc.fabric-api:fabric-api")

    fabricImplementation(libs.fabric.language.kotlin)

    compileOnlyApi("dev.isxander:yet-another-config-lib:${property("dep.yacl")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    fabricApi("dev.isxander:yet-another-config-lib:${property("dep.yacl")}")
    ifPresent("dep.yacl-neoforge") {
        neoforgeApi("dev.isxander:yet-another-config-lib:${property("dep.yacl-neoforge")}")
    }

    ifPresent("dep.mod-menu") {
        fabricModDependency("maven.modrinth:modmenu:$it")
    }

    ifPresent("dep.klf") {
        neoforgeImplementation("dev.nyon:KotlinLangForge:${property("dep.klf")}")
    }

    ifPresent("dep.controlify") {
        commonCompileOnly("dev.isxander:controlify:$it")
    }
}

// Versions without NeoForge cannot compile its platform sources or run the
// common-source verification against a NeoForge Minecraft classpath.
if (!hasProperty("dep.neoforge")) {
    sourceSets.neoforge {
        java.setSrcDirs(emptyList<String>())
        kotlin.setSrcDirs(emptyList<String>())
    }
    tasks.named("compileCommonNeoforgeCheckJava") { enabled = false }
    tasks.named("compileCommonNeoforgeCheckKotlin") { enabled = false }
    tasks.named("verifyCommonNeoforgeOutput") { enabled = false }
}

/// Stonecutter

stonecutter {
    constants {
        put("controlify", hasProperty("dep.controlify"))
    }
}

/// Run configurations

ifPresent("dep.neoforge") {
    runs.register("neoforgeClient") {
        runType("client")
    }
}

/// Metadata file generation

val minecraftRange = property("meta.minecraft-range")!!.toString()
val supportedMinecraftVersions = manifests.minecraftReleasesMatching(minecraftRange)

val commonManifest = manifests.manifest {
    modId = providers.gradleProperty("mod.id")
    version = project.version.toString()
    displayName = providers.gradleProperty("mod.name")
    description = providers.gradleProperty("mod.description")
    authors.add("isXander")
    iconPath = "assets/zoomify/zoomify.png"
    licenses.add("LGPL-3.0-or-later")
    issueTrackerUrl = providers.gradleProperty("mod.issuesUrl")
    sourcesUrl = providers.gradleProperty("mod.sourcesUrl")
    homepage = sourcesUrl

    mixin("zoomify.mixins.json")

    dependency("minecraft", REQUIRED, minecraftRange)
    dependency("yet_another_config_lib_v3", REQUIRED, "*")
}
manifests {
    fabricModJson(sourceSets.fabric.get()) {
        from(commonManifest)

        entrypoint("modmenu", "dev.isxander.zoomify.fabric.integrations.ModMenuIntegration", "kotlin")
        entrypoint("client", "dev.isxander.zoomify.fabric.ZoomifyBootstrap", "kotlin")
        dependency("fabricloader", REQUIRED, "[0.19,)")
    }

    neoForgeModsToml(sourceSets.neoforge.get()) {
        from(commonManifest)

        modLoader = "klf"
        loaderVersion = "[1,)"
    }
}

/// Build settings

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        into("META-INF")
    }
}

// the neoforge main compile check reveals javac inconsistencies with
// incremental compilation and anonymous class constructor parameter LVT
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}
// Mixin 0.17.4 changed ModifyArg/ModifyVariable.at from At to At[]. Align the
// compile APIs so common annotation encoding matches in the NeoForge check.
// Existing Mixin runtimes accept both encodings; leave runtime dependencies unchanged.
configurations.named("neoforgeCompileClasspath") {
    resolutionStrategy.force("net.fabricmc:sponge-mixin:0.17.4+mixin.0.8.7")
}

/// Publishing

publishMods {
    from(rootProject.publishMods)
    dryRun = rootProject.publishMods.dryRun

    version = "$modVersion+mc$minecraftVersion"
    displayName = version.map { "Zoomify $it" }

    file = tasks.universalJar.flatMap { it.archiveFile }
    modLoaders.addAll("fabric", "neoforge")

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("modrinth.id")
        environment = CLIENT_ONLY_SERVER_OPTIONAL
        announcementTitle = "Modrinth ($minecraftVersion)"
        minecraftVersions.addAll(supportedMinecraftVersions)

        requires("fabric-api")
        requires("yacl")
        requires("fabric-language-kotlin")
        requires("kotlin-lang-forge")
        optional("modmenu")
    }

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = providers.gradleProperty("curseforge.id")
        projectSlug = providers.gradleProperty("curseforge.slug")
        client = true
        server = true
        announcementTitle = "Curseforge ($minecraftVersion)"
        minecraftVersions.addAll(supportedMinecraftVersions)

        requires("fabric-api")
        requires("yacl")
        requires("fabric-language-kotlin")
        requires("kotlinlangforge")
        optional("modmenu")
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "zoomify"

            from(components["java"])
            artifact(tasks.universalJar)
            artifact(tasks.universalSourcesJar)
        }
    }
    repositories {
        maven("https://maven.isxander.dev/releases") {
            name = "XanderMaven"
            credentials(PasswordCredentials::class)
        }
    }
}

/// Utilities

fun <T> ifPresent(property: String, block: (String) -> T): T? {
    return if (hasProperty(property)) {
        block(property(property).toString())
    } else {
        null
    }
}
