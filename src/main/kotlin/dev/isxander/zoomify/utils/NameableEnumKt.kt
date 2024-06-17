package dev.isxander.zoomify.utils

import dev.isxander.yacl3.api.NameableEnum
import net.minecraft.network.chat.Component

interface NameableEnumKt : NameableEnum {
    val localisedName: Component

    override fun getDisplayName(): Component = localisedName
}
