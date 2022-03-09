package dev.isxander.zoomify.config

import dev.isxander.settxi.Setting
import dev.isxander.settxi.clothconfig.SettxiGuiWrapper
import dev.isxander.settxi.impl.boolean
import dev.isxander.settxi.impl.int
import dev.isxander.settxi.impl.option
import dev.isxander.zoomify.utils.TransitionType
import dev.isxander.zoomify.utils.mc
import net.minecraft.text.TranslatableText
import java.io.File

object ZoomifySettings : SettxiGuiWrapper(TranslatableText("zoomify.gui.title"), File(mc.runDirectory, "config/zoomify.json")) {
    override val settings = mutableListOf<Setting<*>>()

    var initialZoom by int(4) {
        name = "zoomify.gui.initialZoom.name"
        description = "zoomify.gui.initialZoom.description"
        category = "zoomify.gui.category.behaviour"
        range = 1..10
    }

    var maxScrollZoom by int(75) {
        name = "zoomify.gui.maxScrollZoom.name"
        description = "zoomify.gui.maxScrollZoom.description"
        category = "zoomify.gui.category.behaviour"
        range = 1..100
    }

    var _scrollZoomTransition by option(TransitionType.values().toOptionContainer { it.translationKey }.options[0]) {
        name = "zoomify.gui.scrollZoomTransition.name"
        description = "zoomify.gui.scrollZoomTransition.description"
        category = "zoomify.gui.category.scrolling"
    }
    var scrollZoomTransition: TransitionType
        get() = TransitionType.values()[this._scrollZoomTransition.ordinal]
        set(value) {
            this._scrollZoomTransition = _scrollZoomTransition.container.options[value.ordinal]
        }

    var scrollZoomOppositeTransitionOut by boolean(true) {
        name = "zoomify.gui.scrollZoomOppositeTransitionOut.name"
        description = "zoomify.gui.scrollZoomOppositeTransitionOut.description"
        category = "zoomify.gui.category.scrolling"
    }

    var scrollZoomSpeed by int(50) {
        name = "zoomify.gui.scrollZoomSpeed.name"
        description = "zoomify.gui.scrollZoomSpeed.description"
        category = "zoomify.gui.category.scrolling"
        range = 1..100
    }

    var zoomSpeed by int(50) {
        name = "zoomify.gui.zoomSpeed.name"
        description = "zoomify.gui.zoomSpeed.description"
        category = "zoomify.gui.category.behaviour"
        range = 1..100
    }

    var _zoomTransition by option(TransitionType.values().toOptionContainer { it.translationKey }.options[0]) {
        name = "zoomify.gui.zoomTransition.name"
        description = "zoomify.gui.zoomTransition.description"
        category = "zoomify.gui.category.behaviour"
    }
    var zoomTransition: TransitionType
        get() = TransitionType.values()[this._zoomTransition.ordinal]
        set(value) {
            this._zoomTransition = _zoomTransition.container.options[value.ordinal]
        }

    var zoomOppositeTransitionOut by boolean(true) {
        name = "zoomify.gui.zoomOppositeTransitionOut.name"
        description = "zoomify.gui.zoomOppositeTransitionOut.description"
        category = "zoomify.gui.category.behaviour"
    }

    var _zoomKeyBehaviour by option(ZoomKeyBehaviour.values().toOptionContainer { it.translationKey }.options[0]) {
        name = "zoomify.gui.zoomKeyBehaviour.name"
        description = "zoomify.gui.zoomKeyBehaviour.description"
        category = "zoomify.gui.category.controls"
    }
    var zoomKeyBehaviour: ZoomKeyBehaviour
        get() = ZoomKeyBehaviour.values()[this._zoomKeyBehaviour.ordinal]
        set(value) {
            this._zoomKeyBehaviour = _zoomKeyBehaviour.container.options[value.ordinal]
        }

    var cinematicCam by boolean(false) {
        name = "zoomify.gui.cinematicCam.name"
        description = "zoomify.gui.cinematicCam.description"
        category = "zoomify.gui.category.controls"
    }

    init {
        load()
    }
}
