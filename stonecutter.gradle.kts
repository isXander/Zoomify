plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active file("versions/current")

stonecutter parameters {
    fun String.propDefined() = project.findProperty(this)?.toString()?.isNotBlank() ?: false

    constants {
        put("mod-menu", "deps.modMenu".propDefined())
    }
}
