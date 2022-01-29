package dev.isxander.zoomify.utils

/**
 * Converts 'SOME_ENUM' to 'Some Enum'
 */
fun String.formatEnum(): String {
    return split("_").joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }.trimEnd()
}
