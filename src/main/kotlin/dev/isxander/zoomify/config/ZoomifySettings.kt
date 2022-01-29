package dev.isxander.zoomify.config

import dev.isxander.zoomify.utils.TransitionType
import dev.isxander.zoomify.utils.formatEnum
import dev.isxander.zoomify.utils.mc
import gg.essential.vigilance.Vigilant
import java.io.File

object ZoomifySettings : Vigilant(File(mc.runDirectory, "config/zoomify.toml"), "Zoomify") {
    var initialZoom = 4f
    var scrollAmount = 0.75f

    var zoomSpeed = 0.5f
    private var _zoomTransition = TransitionType.LINEAR.ordinal
    var zoomTransition: TransitionType
        get() = TransitionType.values()[this._zoomTransition]
        set(value) {
            this._zoomTransition = value.ordinal
        }

    init {
        category("zoomify.gui.category.main") {
            decimalSlider(::initialZoom, "zoomify.gui.initialZoom.name", "zoomify.gui.initialZoom.description", min = 1f, max = 10f)
            percentSlider(::scrollAmount, "zoomify.gui.scrollAmount.name", "zoomify.gui.scrollAmount.description")

            percentSlider(::zoomSpeed, "zoomify.gui.zoomSpeed.name", "zoomify.gui.zoomSpeed.description")
            selector(::_zoomTransition, "zoomify.gui.zoomTransition.name", "zoomify.gui.zoomTransition.description", TransitionType.values().map { it.translationKey })
        }

        initialize()

        hidePropertyIf(::zoomSpeed) { zoomTransition == TransitionType.INSTANT }
    }
}
