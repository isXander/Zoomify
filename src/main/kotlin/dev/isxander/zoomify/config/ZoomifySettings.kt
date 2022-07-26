package dev.isxander.zoomify.config

import dev.isxander.settxi.gui.clothGui
import dev.isxander.settxi.impl.boolean
import dev.isxander.settxi.impl.enum
import dev.isxander.settxi.impl.float
import dev.isxander.settxi.impl.int
import dev.isxander.settxi.serialization.PrimitiveType
import dev.isxander.settxi.serialization.SettxiConfigKotlinx
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.utils.TransitionType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object ZoomifySettings : SettxiConfigKotlinx(FabricLoader.getInstance().configDir.resolve("zoomify.json")) {
    private var needsSaving = false

    var initialZoom by int(4) {
        name = "zoomify.gui.initialZoom.name"
        description = "zoomify.gui.initialZoom.description"
        category = "zoomify.gui.category.behaviour"
        range = 1..10
    }

    var zoomInTime by float(2f) {
        name = "zoomify.gui.zoomInTime.name"
        description = "zoomify.gui.zoomInTime.description"
        category = "zoomify.gui.category.behaviour"
        range = 0.1f..20f
    }

    var zoomOutTime by float(0.5f) {
        name = "zoomify.gui.zoomOutTime.name"
        description = "zoomify.gui.zoomOutTime.description"
        category = "zoomify.gui.category.behaviour"
        range = 0.1f..20f
    }

    var zoomTransition by enum(TransitionType.EASE_OUT_EXP) {
        name = "zoomify.gui.zoomTransition.name"
        description = "zoomify.gui.zoomTransition.description"
        category = "zoomify.gui.category.behaviour"
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
        category = "zoomify.gui.category.behaviour"
    }

    var scrollZoom by boolean(true) {
        name = "zoomify.gui.scrollZoom.name"
        description = "zoomify.gui.scrollZoom.description"
        category = "zoomify.gui.category.scrolling"
    }

    var scrollZoomAmount by int(1) {
        name = "zoomify.gui.scrollZoomAmount.name"
        description = "zoomify.gui.scrollZoomAmount.description"
        category = "zoomify.gui.category.scrolling"
        range = 1..5
    }

    var scrollZoomSmoothness by int(0) {
        name = "zoomify.gui.scrollZoomSmoothness.name"
        description = "zoomify.gui.scrollZoomSmoothness.description"
        category = "zoomify.gui.category.scrolling"
        range = 0..100
    }

    var zoomKeyBehaviour by enum(ZoomKeyBehaviour.HOLD) {
        name = "zoomify.gui.zoomKeyBehaviour.name"
        description = "zoomify.gui.zoomKeyBehaviour.description"
        category = "zoomify.gui.category.controls"
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
        category = "zoomify.gui.category.controls"
        range = 0..200
    }

    var relativeViewBobbing by boolean(true) {
        name = "zoomify.gui.relativeViewBobbing.name"
        description = "zoomify.gui.relativeViewBobbing.description"
        category = "zoomify.gui.category.controls"
    }

    var cinematicCam by boolean(false) {
        name = "zoomify.gui.cinematicCam.name"
        description = "zoomify.gui.cinematicCam.description"
        category = "zoomify.gui.category.controls"
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
