package dev.isxander.zoomify.config

import dev.isxander.settxi.impl.SettingDisplayName
import net.minecraft.util.StringRepresentable

enum class SpyglassBehaviour(override val displayName: String) : SettingDisplayName, StringRepresentable {
    COMBINE("zoomify.spyglass_behaviour.combine"),
    OVERRIDE("zoomify.spyglass_behaviour.override"),
    ONLY_ZOOM_WHILE_HOLDING("zoomify.spyglass_behaviour.only_zoom_while_holding"),
    ONLY_ZOOM_WHILE_CARRYING("zoomify.spyglass_behaviour.only_zoom_while_carrying");

    override fun getSerializedName(): String = name.lowercase()

    companion object {
        val CODEC = StringRepresentable.fromEnum(::values)
    }
}

enum class OverlayVisibility(override val displayName: String) : SettingDisplayName, StringRepresentable {
    NEVER("zoomify.overlay_visibility.never"),
    HOLDING("zoomify.overlay_visibility.holding"),
    CARRYING("zoomify.overlay_visibility.carrying"),
    ALWAYS("zoomify.overlay_visibility.always");

    override fun getSerializedName(): String = name.lowercase()

    companion object {
        val CODEC = StringRepresentable.fromEnum(::values)
    }
}

enum class SoundBehaviour(override val displayName: String) : SettingDisplayName, StringRepresentable {
    NEVER("zoomify.sound_behaviour.never"),
    ALWAYS("zoomify.sound_behaviour.always"),
    ONLY_SPYGLASS("zoomify.sound_behaviour.only_spyglass"),
    WITH_OVERLAY("zoomify.sound_behaviour.with_overlay");

    override fun getSerializedName(): String = name.lowercase()

    companion object {
        val CODEC = StringRepresentable.fromEnum(::values)
    }
}
