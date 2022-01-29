package dev.isxander.zoomify.config

import dev.isxander.zoomify.utils.TransitionType
import dev.isxander.zoomify.utils.formatEnum
import dev.isxander.zoomify.utils.mc
import gg.essential.vigilance.Vigilant
import java.io.File

object ZoomifySettings : Vigilant(File(mc.runDirectory, "config/zoomify.toml"), "Zoomify") {
    var initialZoom = 4f

    var maxScrollZoom = 0.75f
    private var _scrollZoomTransition = TransitionType.LINEAR.ordinal
    var scrollZoomTransition: TransitionType
        get() = TransitionType.values()[this._scrollZoomTransition]
        set(value) {
            this._scrollZoomTransition = value.ordinal
        }
    var scrollZoomSpeed = 0.5f

    var zoomSpeed = 0.5f
    private var _zoomTransition = TransitionType.LINEAR.ordinal
    var zoomTransition: TransitionType
        get() = TransitionType.values()[this._zoomTransition]
        set(value) {
            this._zoomTransition = value.ordinal
        }


    private var _zoomKeyBehaviour = ZoomKeyBehaviour.HOLD.ordinal
    var zoomKeyBehaviour: ZoomKeyBehaviour
        get() = ZoomKeyBehaviour.values()[this._zoomKeyBehaviour]
        set(value) {
            this._zoomKeyBehaviour = value.ordinal
        }

    var cinematicCam = false

    init {
        category("zoomify.gui.category.behaviour") {
            decimalSlider(::initialZoom, "zoomify.gui.initialZoom.name", "zoomify.gui.initialZoom.description", min = 1f, max = 10f)
            percentSlider(::zoomSpeed, "zoomify.gui.zoomSpeed.name", "zoomify.gui.zoomSpeed.description")
            selector(::_zoomTransition, "zoomify.gui.zoomTransition.name", "zoomify.gui.zoomTransition.description", TransitionType.values().map { it.translationKey })

            subcategory("zoomify.gui.subcategory.scrolling") {
                percentSlider(::maxScrollZoom, "zoomify.gui.maxScrollZoom.name", "zoomify.gui.maxScrollZoom.description")
                percentSlider(::scrollZoomSpeed, "zoomify.gui.scrollZoomSpeed.name", "zoomify.gui.scrollZoomSpeed.description")
                selector(::_scrollZoomTransition, "zoomify.gui.scrollZoomTransition.name", "zoomify.gui.scrollZoomTransition.description", TransitionType.values().map { it.translationKey })
            }
        }

        category("zoomify.gui.category.controls") {
            selector(::_zoomKeyBehaviour, "zoomify.gui.zoomKeyBehaviour.name", "zoomify.gui.zoomKeyBehaviour.description", ZoomKeyBehaviour.values().map { it.translationKey })
            switch(::cinematicCam, "zoomify.gui.cinematicCam.name", "zoomify.gui.cinematicCam.description")
        }

        initialize()

        hidePropertyIf(::zoomSpeed) { zoomTransition == TransitionType.INSTANT }
    }
}
