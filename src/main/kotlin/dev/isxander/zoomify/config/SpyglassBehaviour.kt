package dev.isxander.zoomify.config

import dev.isxander.settxi.impl.SettingDisplayName

enum class SpyglassBehaviour(override val displayName: String) : SettingDisplayName {
    COMBINE("zoomify.spyglass_behaviour.combine"),
    OVERRIDE("zoomify.spyglass_behaviour.override"),
    ONLY_ZOOM_WHILE_HOLDING("zoomify.spyglass_behaviour.only_zoom_while_holding"),
    ONLY_ZOOM_WHILE_CARRYING("zoomify.spyglass_behaviour.only_zoom_while_carrying")
}

enum class OverlayVisibility(override val displayName: String) : SettingDisplayName {
    DEFAULT("zoomify.overlay_visibility.default"),
    HOLDING("zoomify.overlay_visibility.holding"),
    CARRYING("zoomify.overlay_visibility.carrying"),
    ALWAYS("zoomify.overlay_visibility.always")
}
