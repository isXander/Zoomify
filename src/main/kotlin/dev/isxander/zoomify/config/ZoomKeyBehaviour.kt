package dev.isxander.zoomify.config

import dev.isxander.settxi.impl.SettingDisplayName
import net.minecraft.util.StringRepresentable

enum class ZoomKeyBehaviour(override val displayName: String) : SettingDisplayName, StringRepresentable {
    HOLD("zoomify.zoom_key_behaviour.hold"),
    TOGGLE("zoomify.zoom_key_behaviour.toggle");

    override fun getSerializedName(): String = name.lowercase()

    companion object {
        val CODEC = StringRepresentable.fromEnum(::values)
    }
}
