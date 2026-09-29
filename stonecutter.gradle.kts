plugins {
    id("dev.kikugie.stonecutter")
    alias(libs.plugins.mod.publish.plugin)
    alias(libs.plugins.spotless)
}
stonecutter active file("versions/current")

val modVersion = providers.gradleProperty("mod.version")
version = modVersion.get()

repositories {
    mavenCentral()
}

val printVersion = tasks.register("printVersion") {
    group = "zoomify/internal"

    inputs.property("version", version)

    doLast {
        logger.quiet("ZOOMIFY_VERSION=${inputs.properties["version"]}")
    }
}

val publishTargets = stonecutter.versions.joinToString(separator = "\n") { "- ${it.project}" }
val changelogContents = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md"))
    .asText
    .zip(modVersion) { changelog, version ->
        changelog.replace("{version}", version)
    }
    .zip(provider { publishTargets }) { changelog, targets ->
        changelog.replace(
            "{targets}",
            targets
        )
    }

publishMods {
    dryRun = false

    version = modVersion

    changelog = changelogContents

    type = modVersion.map { version ->
        when {
            "alpha" in version -> ALPHA
            "beta" in version -> BETA
            else -> STABLE
        }
    }

    discord {
        webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK_URL")
        setPlatformsAllFrom(*stonecutter.versions.map { project(it.project) }.toTypedArray())
        avatarUrl = providers.gradleProperty("discord.image-url")
        content = changelog.zip(providers.gradleProperty("discord.ping")) { changelog, ping ->
            val pingPostfix = "\n\n$ping"
            val truncationMarker = "... (truncated)"
            val maxChars = 2_000

            val availableChangelogLength = maxChars - pingPostfix.length

            val finalChangelog = if (changelog.length > availableChangelogLength) {
                val availableContentLength =
                    (availableChangelogLength - truncationMarker.length).coerceAtLeast(0)

                changelog.take(availableContentLength) +
                        truncationMarker.take(availableChangelogLength)
            } else {
                changelog
            }

            finalChangelog + pingPostfix
        }
        username = "Zoomify"
    }
}

spotless {
    java {
        target("src/**/*.java")
        licenseHeaderFile(rootProject.layout.projectDirectory.file("HEADER"))

        trimTrailingWhitespace()
        endWithNewline()
        formatAnnotations()
        leadingSpacesToTabs(4)
    }

    kotlin {
        target("src/**/*.kt")
        licenseHeaderFile(rootProject.layout.projectDirectory.file("HEADER"))

        trimTrailingWhitespace()
        endWithNewline()
        leadingSpacesToTabs(4)
    }
}
