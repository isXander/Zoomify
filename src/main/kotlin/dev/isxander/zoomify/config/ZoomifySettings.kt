package dev.isxander.zoomify.config

import dev.isxander.settxi.gui.clothGui
import dev.isxander.settxi.impl.*
import dev.isxander.settxi.serialization.PrimitiveType
import dev.isxander.settxi.serialization.SettxiConfigKotlinx
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.utils.TransitionType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object ZoomifySettings : SettxiConfigKotlinx(FabricLoader.getInstance().configDir.resolve("zoomify.json")) {
    private const val BEHAVIOUR = "zoomify.gui.category.behaviour"
    private const val SCROLLING = "zoomify.gui.category.scrolling"
    private const val CONTROLS = "zoomify.gui.category.controls"
    private const val SPYGLASS = "zoomify.gui.category.spyglass"

    private var needsSaving = false

    var initialZoom by int(4) {
        name = "zoomify.gui.initialZoom.name"
        description = "zoomify.gui.initialZoom.description"
        category = BEHAVIOUR
        range = 1..10
    }

    var zoomInTime by double(2.0) {
        name = "zoomify.gui.zoomInTime.name"
        description = "zoomify.gui.zoomInTime.description"
        category = BEHAVIOUR
        range = 0.1..20.0
    }

    var zoomOutTime by double(0.5) {
        name = "zoomify.gui.zoomOutTime.name"
        description = "zoomify.gui.zoomOutTime.description"
        category = BEHAVIOUR
        range = 0.1..20.0
    }

    var zoomTransition by enum(TransitionType.EASE_OUT_EXP) {
        name = "zoomify.gui.zoomTransition.name"
        description = "zoomify.gui.zoomTransition.description"
        category = BEHAVIOUR
        migrator { type ->
            if (type.isString) {
                Zoomify.LOGGER.info("Migrating transition type from string to int")
                PrimitiveType.of(TransitionType.values().find { transition ->
                    transition.displayName.lowercase().replace(Regex("\\W+"), "_")
                        .trim { it == '_' || it.isWhitespace() } == type.string
                }!!.ordinal).also { needsSaving = true }
            } else type
        }
    }

    var zoomOppositeTransitionOut by boolean(true) {
        name = "zoomify.gui.zoomOppositeTransitionOut.name"
        description = "zoomify.gui.zoomOppositeTransitionOut.description"
        category = BEHAVIOUR
    }

    var scrollZoom by boolean(true) {
        name = "zoomify.gui.scrollZoom.name"
        description = "zoomify.gui.scrollZoom.description"
        category = SCROLLING
    }

    var scrollZoomAmount by int(2) {
        name = "zoomify.gui.scrollZoomAmount.name"
        description = "zoomify.gui.scrollZoomAmount.description"
        category = SCROLLING
        range = 1..5
        migrator { type ->
            if (!type.isInt) {
                PrimitiveType.of(default)
            }
            type
        }
    }

    var scrollZoomSmoothness by int(70) {
        name = "zoomify.gui.scrollZoomSmoothness.name"
        description = "zoomify.gui.scrollZoomSmoothness.description"
        category = SCROLLING
        range = 0..100
    }

    var zoomKeyBehaviour by enum(ZoomKeyBehaviour.HOLD) {
        name = "zoomify.gui.zoomKeyBehaviour.name"
        description = "zoomify.gui.zoomKeyBehaviour.description"
        category = CONTROLS
        migrator { type ->
            if (type.isString) {
                Zoomify.LOGGER.info("Migrating transition type from string to int")
                PrimitiveType.of(ZoomKeyBehaviour.values().find { keyBehaviour ->
                    keyBehaviour.displayName.lowercase()
                        .replace(Regex("\\W+"), "_")
                        .trim { it == '_' || it.isWhitespace() } == type.string
                }!!.ordinal).also { needsSaving = true }
            } else type
        }
    }

    var relativeSensitivity by int(100) {
        name = "zoomify.gui.relativeSensitivity.name"
        description = "zoomify.gui.relativeSensitivity.description"
        category = CONTROLS
        range = 0..100
    }

    var relativeViewBobbing by boolean(true) {
        name = "zoomify.gui.relativeViewBobbing.name"
        description = "zoomify.gui.relativeViewBobbing.description"
        category = CONTROLS
    }

    var cinematicCam by boolean(false) {
        name = "zoomify.gui.cinematicCam.name"
        description = "zoomify.gui.cinematicCam.description"
        category = CONTROLS
    }

    var spyglassBehaviour by enum(SpyglassBehaviour.COMBINE) {
        name = "zoomify.gui.spyglassBehaviour.name"
        description = "zoomify.gui.spyglassBehaviour.description"
        category = SPYGLASS
    }

    var spyglassOverlayVisibility by enum(OverlayVisibility.HOLDING) {
        name = "zoomify.gui.spyglassOverlayVisibility.name"
        description = "zoomify.gui.spyglassOverlayVisibility.description"
        category = SPYGLASS
    }

    var spyglassSoundBehaviour by enum(SoundBehaviour.WITH_OVERLAY) {
        name = "zoomify.gui.spyglassSoundBehaviour.name"
        description = "zoomify.gui.spyglassSoundBehaviour.description"
        category = SPYGLASS
    }

    init {
        import()
        if (needsSaving) {
            export()
            needsSaving = false
        }
    }

    fun gui(parent: Screen? = null) =
        clothGui(Text.translatable("zoomify.gui.title"), parent)
}
