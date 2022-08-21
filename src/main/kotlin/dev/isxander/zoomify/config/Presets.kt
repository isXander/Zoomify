package dev.isxander.zoomify.config

import dev.isxander.settxi.Setting
import dev.isxander.settxi.impl.SettingDisplayName
import dev.isxander.zoomify.utils.TransitionType

enum class Presets(override val displayName: String, val apply: ZoomifySettings.() -> Unit) : SettingDisplayName {
    DEFAULT("zoomify.gui.preset.default", {
        this.settings.forEach(Setting<*>::reset)
    }),
    OPTIFINE("zoomify.gui.preset.optifine", {
        DEFAULT.apply(this)

        this.zoomInTransition = TransitionType.INSTANT
        this.zoomOutTransition = TransitionType.INSTANT
        this.scrollZoom = false
        this.relativeSensitivity = 0
        this.relativeViewBobbing = false
        this.cinematicCam = true
    }),
    OK_ZOOMER("zoomify.gui.preset.ok_zoomer", {
        DEFAULT.apply(this)

        this.zoomInTime = 0.25
        this.zoomOutTime = 0.25
        this.relativeSensitivity = 50
        this.relativeViewBobbing = false
        this.scrollZoomSmoothness = 25
        this.linearLikeSteps = false
    }),
    SMOOTH("zoomify.gui.preset.smooth", {
        DEFAULT.apply(this)

        this.zoomInTime = 0.25
        this.zoomOutTime = 0.30
        this.relativeSensitivity = 50
        this.relativeViewBobbing = false
        this.scrollZoomSmoothness = 25
        this.linearLikeSteps = false
    }),
}
