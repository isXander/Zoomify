plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.3" /* [SC] DO NOT EDIT */

stonecutter.configureEach {
    fun String.propDefined() = project.findProperty(this)?.toString()?.isNotBlank() ?: false

    consts(listOf(
        "mod-menu" to "deps.modMenu".propDefined()
    ))
}

stonecutter registerChiseled tasks.register("buildAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("releaseAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("releaseMod")
}
