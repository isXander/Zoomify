package dev.isxander.zoomify.config

import dev.isxander.yacl3.config.v3.ConfigEntry
import dev.isxander.yacl3.config.v3.default
import dev.isxander.yacl3.config.v3.value
import dev.isxander.zoomify.utils.TransitionType
import net.minecraft.network.chat.Component

enum class Presets(val displayName: Component, val apply: ZoomifySettings.() -> Unit) {
    Default("zoomify.gui.preset.default", {
        this.allSettings.forEach { it.setToDefault() }
    }),
    Optifine("zoomify.gui.preset.optifine", {
        Default.apply(this)

        this.zoomInTransition.value = TransitionType.INSTANT
        this.zoomOutTransition.value = TransitionType.INSTANT
        this.scrollZoom.value = false
        this.relativeSensitivity.value = 0
        this.relativeViewBobbing.value = false
        this.cinematicCamera.value = 100
    }),
    OkZoomer("zoomify.gui.preset.ok_zoomer", {
        Default.apply(this)

        this.zoomInTime.value = 0.25
        this.zoomOutTime.value = 0.25
        this.relativeSensitivity.value = 50
        this.relativeViewBobbing.value = false
        this.scrollZoomSmoothness.value = 25
        this.linearLikeSteps.value = false
    });

    constructor(displayName: String, apply: ZoomifySettings.() -> Unit)
            : this(Component.translatable(displayName), apply)
}

private fun <T> ConfigEntry<T>.setToDefault() = set(default)
