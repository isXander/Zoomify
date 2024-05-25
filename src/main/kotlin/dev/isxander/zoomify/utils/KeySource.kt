package dev.isxander.zoomify.utils

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping

interface KeySource {
    val justPressed: Boolean

    val isDown: Boolean
}

class MultiKeySource(sources: List<KeySource>) : KeySource {
    private val sources = sources.toMutableList()

    constructor(vararg sources: KeySource) : this(sources.toList())
    constructor() : this(emptyList())

    override val justPressed: Boolean
        get() = sources.any { it.justPressed }

    override val isDown: Boolean
        get() = sources.any { it.isDown }

    fun addSource(source: KeySource) {
        sources.add(source)
    }
}

fun KeyMapping.toKeySource(register: Boolean = false) = object : KeySource {
    override val justPressed: Boolean
        get() = this@toKeySource.consumeClick()

    override val isDown: Boolean
        get() = this@toKeySource.isDown
}.also { if (register) KeyBindingHelper.registerKeyBinding(this) }
