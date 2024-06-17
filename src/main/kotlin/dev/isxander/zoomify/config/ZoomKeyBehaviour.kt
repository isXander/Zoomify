package dev.isxander.zoomify.config

import dev.isxander.zoomify.utils.NameableEnumKt
import net.minecraft.network.chat.Component
import net.minecraft.util.StringRepresentable

enum class ZoomKeyBehaviour(override val localisedName: Component) : NameableEnumKt, StringRepresentable {
    HOLD("zoomify.zoom_key_behaviour.hold"),
    TOGGLE("zoomify.zoom_key_behaviour.toggle");

    override fun getSerializedName(): String = name.lowercase()

    constructor(localisedName: String) : this(Component.translatable(localisedName))

    companion object {
        val CODEC = StringRepresentable.fromEnum(::values)
    }
}
