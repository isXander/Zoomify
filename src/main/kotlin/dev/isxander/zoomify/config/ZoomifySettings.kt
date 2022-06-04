package dev.isxander.zoomify.config

import dev.isxander.settxi.Setting
import dev.isxander.settxi.clothconfig.SettxiGuiWrapper
import dev.isxander.settxi.impl.boolean
import dev.isxander.settxi.impl.enum
import dev.isxander.settxi.impl.int
import dev.isxander.settxi.serialization.PrimitiveType
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.utils.TransitionType
import dev.isxander.zoomify.utils.mc
import net.minecraft.text.Text
import java.io.File

object ZoomifySettings : SettxiGuiWrapper(Text.translatable("zoomify.gui.title"), File(mc.runDirectory, "config/zoomify.json")) {
    override val settings = mutableListOf<Setting<*>>()

    private var needsSaving = false
    val transitionTypeMigrator: (PrimitiveType) -> PrimitiveType = { type ->
        if (type.isString) {
            Zoomify.LOGGER.info("Migrating transition type from string to int")
            PrimitiveType.of(TransitionType.values().find { transition ->
                transition.translationKey.lowercase().replace(Regex("\\W+"), "_")
                    .trim { it == '_' || it.isWhitespace() } == type.string
            }!!.ordinal).also { needsSaving = true }
        } else type
    }

    val zoomKeyTypeMigrator: (PrimitiveType) -> PrimitiveType = { type ->
        if (type.isString) {
            Zoomify.LOGGER.info("Migrating transition type from string to int")
            PrimitiveType.of(ZoomKeyBehaviour.values().find { keyBehaviour ->
                keyBehaviour.translationKey.lowercase()
                    .replace(Regex("\\W+"), "_")
                    .trim { it == '_' || it.isWhitespace() } == type.string
            }!!.ordinal).also { needsSaving = true }
        } else type
    }

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

    var zoomTransition by enum(TransitionType.EASE_OUT_EXP) {
        name = "zoomify.gui.zoomTransition.name"
        description = "zoomify.gui.zoomTransition.description"
        category = "zoomify.gui.category.behaviour"
        nameProvider = { it.translationKey }
        migrator(transitionTypeMigrator)
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
        range = 1..200
    }

    var scrollZoomTransition by enum(TransitionType.LINEAR) {
        name = "zoomify.gui.scrollZoomTransition.name"
        description = "zoomify.gui.scrollZoomTransition.description"
        category = "zoomify.gui.category.scrolling"
        nameProvider = { it.translationKey }
        migrator(transitionTypeMigrator)
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
        nameProvider = { it.translationKey }
        migrator(zoomKeyTypeMigrator)
    }

    var relativeSensitivity by boolean(false) {
        name = "zoomify.gui.relativeSensitivity.name"
        description = "zoomify.gui.relativeSensitivity.description"
        category = "zoomify.gui.category.controls"
    }

    var cinematicCam by boolean(false) {
        name = "zoomify.gui.cinematicCam.name"
        description = "zoomify.gui.cinematicCam.description"
        category = "zoomify.gui.category.controls"
    }

    init {
        load()
        if (needsSaving) {
            save()
            needsSaving = false
        }
    }
}
