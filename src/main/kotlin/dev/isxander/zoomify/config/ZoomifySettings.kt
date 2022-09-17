package dev.isxander.zoomify.config

import dev.isxander.settxi.impl.boolean
import dev.isxander.settxi.impl.enum
import dev.isxander.settxi.impl.int
import dev.isxander.settxi.serialization.PrimitiveType
import dev.isxander.settxi.serialization.SettxiFileConfig
import dev.isxander.settxi.serialization.kotlinxSerializer
import dev.isxander.zoomify.config.cloth.clothGui
import dev.isxander.zoomify.utils.TransitionType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.TranslatableText

object ZoomifySettings : SettxiFileConfig(FabricLoader.getInstance().configDir.resolve("zoomify.json"), kotlinxSerializer()) {
    private var needsSaving = false

    var initialZoom by int(4) {
        name = "zoomify.gui.initialZoom.name"
        description = "zoomify.gui.initialZoom.description"
        category = "zoomify.gui.category.behaviour"
        range = 1..10
    }

    var zoomSpeed by int(50) {
        name = "zoomify.gui.zoomSpeed.name"
        description = "zoomify.gui.zoomSpeed.description"
        category = "zoomify.gui.category.behaviour"
        range = 1..150
    }

    var zoomTransition by enum(TransitionType.INSTANT) {
        name = "zoomify.gui.zoomTransition.name"
        description = "zoomify.gui.zoomTransition.description"
        category = "zoomify.gui.category.behaviour"

        migrator { type ->
            if (type.primitive.isString) {
                PrimitiveType.of(TransitionType.values().find { transition ->
                    transition.translationKey.lowercase().replace(Regex("\\W+"), "_")
                        .trim { it == '_' || it.isWhitespace() } == type.primitive.string
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
        description = "zoomify.gui.maxScrollZoom.description"
        category = "zoomify.gui.category.scrolling"
    }

    var maxScrollZoom by int(75) {
        name = "zoomify.gui.maxScrollZoom.name"
        description = "zoomify.gui.maxScrollZoom.description"
        category = "zoomify.gui.category.scrolling"
        range = 1..100
    }

    var scrollZoomTransition by enum(TransitionType.INSTANT) {
        name = "zoomify.gui.scrollZoomTransition.name"
        description = "zoomify.gui.scrollZoomTransition.description"
        category = "zoomify.gui.category.scrolling"

        migrator { type ->
            if (type.primitive.isString) {
                PrimitiveType.of(TransitionType.values().find { transition ->
                    transition.translationKey.lowercase().replace(Regex("\\W+"), "_")
                        .trim { it == '_' || it.isWhitespace() } == type.primitive.string
                }!!.ordinal).also { needsSaving = true }
            } else type
        }
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
        range = 1..150
    }

    var zoomKeyBehaviour by enum(ZoomKeyBehaviour.HOLD) {
        name = "zoomify.gui.zoomKeyBehaviour.name"
        description = "zoomify.gui.zoomKeyBehaviour.description"
        category = "zoomify.gui.category.controls"

        migrator { type ->
            if (type.primitive.isString) {
                PrimitiveType.of(ZoomKeyBehaviour.values().find { transition ->
                    transition.translationKey.lowercase().replace(Regex("\\W+"), "_")
                        .trim { it == '_' || it.isWhitespace() } == type.primitive.string
                }!!.ordinal).also { needsSaving = true }
            } else type
        }
    }

    var relativeSensitivity by boolean(false) {
        name = "zoomify.gui.relativeSensitivity.name"
        description = "zoomify.gui.relativeSensitivity.description"
        category = "zoomify.gui.category.controls"

        migrator { type ->
            if (type.primitive.isInt) {
                needsSaving = true
                if (type.primitive.int > 0)
                    PrimitiveType.of(true)
                else
                    PrimitiveType.of(false)
            }

            type
        }
    }

    var cinematicCam by boolean(false) {
        name = "zoomify.gui.cinematicCam.name"
        description = "zoomify.gui.cinematicCam.description"
        category = "zoomify.gui.category.controls"

        migrator { type ->
            if (type.primitive.isInt) {
                needsSaving = true
                if (type.primitive.int > 0)
                    PrimitiveType.of(true)
                else
                    PrimitiveType.of(false)
            }

            type
        }
    }

    init {
        import()

        if (needsSaving) {
            export()
            needsSaving = false
        }
    }

    fun gui(parent: Screen?) =
        clothGui(TranslatableText("zoomify.gui.title"), parent)
}
