package dev.isxander.zoomify.config

import dev.isxander.settxi.impl.SettingDisplayName

enum class ZoomKeyBehaviour(override val displayName: String) : SettingDisplayName {
    HOLD("zoomify.zoom_key_behaviour.hold"),
    TOGGLE("zoomify.zoom_key_behaviour.toggle")
}
