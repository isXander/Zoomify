package dev.isxander.zoomify.integrations

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.version.VersionPredicate

fun constrainModVersionIfLoaded(modId: String, constraint: String) {
    val predicate = VersionPredicate.parse(constraint)

    val modContainer = FabricLoader.getInstance().getModContainer(modId)
    if (modContainer.isPresent) {
        val version = modContainer.get().metadata.version
        if (!predicate.test(version)) {
            throw IllegalStateException("Mod $modId is not constrained to version $constraint")
        }
    }
}
