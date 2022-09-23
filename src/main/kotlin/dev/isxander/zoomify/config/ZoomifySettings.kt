package dev.isxander.zoomify.config

import dev.isxander.settxi.gui.yacl.*
import dev.isxander.settxi.impl.*
import dev.isxander.settxi.serialization.PrimitiveType
import dev.isxander.settxi.serialization.SettxiFileConfig
import dev.isxander.settxi.serialization.kotlinxSerializer
import dev.isxander.yacl.api.Option
import dev.isxander.yacl.api.OptionFlag
import dev.isxander.yacl.api.utils.OptionUtils
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.utils.TransitionType
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.toast.SystemToast
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.io.path.notExists

object ZoomifySettings : SettxiFileConfig(
    FabricLoader.getInstance().configDir.resolve("zoomify.json"),
    kotlinxSerializer(Json { prettyPrint = true })
) {
    private const val BEHAVIOUR = "zoomify.gui.category.behaviour"
    private const val SCROLLING = "zoomify.gui.category.scrolling"
    private const val CONTROLS = "zoomify.gui.category.controls"
    private const val SPYGLASS = "zoomify.gui.category.spyglass"
    private const val MISC = "zoomify.gui.category.misc"

    private var needsSaving = false

    var initialZoom by int(4) {
        name = "zoomify.gui.initialZoom.name"
        description = "zoomify.gui.initialZoom.description"
        category = BEHAVIOUR
        range = 1..10

        yaclValueFormatter = { Text.of("%dx".format(it)) }
        yaclSliderInterval = 1
    }

    var zoomInTime by double(1.0) {
        name = "zoomify.gui.zoomInTime.name"
        description = "zoomify.gui.zoomInTime.description"
        category = BEHAVIOUR
        range = 0.1..5.0
        yaclValueFormatter = { Text.of("%.1f secs".format(it) ) }
        yaclSliderInterval = 0.1
    }

    var zoomOutTime by double(0.5) {
        name = "zoomify.gui.zoomOutTime.name"
        description = "zoomify.gui.zoomOutTime.description"
        category = BEHAVIOUR
        range = 0.1..5.0
        yaclValueFormatter = { Text.of("%.1f secs".format(it) ) }
        yaclSliderInterval = 0.1
    }

    var zoomInTransition by enum(TransitionType.EASE_OUT_EXP) {
        name = "zoomify.gui.zoomInTransition.name"
        description = "zoomify.gui.zoomInTransition.description"
        category = BEHAVIOUR
        defaultSerializedValue = { _, category ->
            if (category?.containsKey("zoomify_gui_zoomtransition_name") == true) {
                needsSaving = true
                category["zoomify_gui_zoomtransition_name"]!!
            } else {
                PrimitiveType.of(default.ordinal)
            }
        }
        migrator { type ->
            if (type.primitive.isString) {
                Zoomify.LOGGER.info("Migrating transition type from string to int")
                PrimitiveType.of(TransitionType.values().find { transition ->
                    transition.displayName.lowercase().replace(Regex("\\W+"), "_")
                        .trim { it == '_' || it.isWhitespace() } == type.primitive.string
                }!!.ordinal).also { needsSaving = true }
            } else type
        }
    }

    var zoomOutTransition by enum(TransitionType.EASE_OUT_EXP) {
        name = "zoomify.gui.zoomOutTransition.name"
        description = "zoomify.gui.zoomOutTransition.description"
        category = BEHAVIOUR
        defaultSerializedValue = { _, category ->
            if (category?.containsKey("zoomify_gui_zoomtransition_name") == true) {
                needsSaving = true
                category["zoomify_gui_zoomtransition_name"]!!
            } else {
                PrimitiveType.of(default.ordinal)
            }
        }
        migrator { type ->
            if (type.primitive.isString) {
                Zoomify.LOGGER.info("Migrating transition type from string to int")
                PrimitiveType.of(TransitionType.values().find { transition ->
                    transition.displayName.lowercase().replace(Regex("\\W+"), "_")
                        .trim { it == '_' || it.isWhitespace() } == type.primitive.string
                }!!.ordinal).also { needsSaving = true }
            } else type
        }
        modifyGet { it.opposite() }
    }

    var affectHandFov by boolean(true) {
        name = "zoomify.gui.affectHandFov.name"
        description = "zoomify.gui.affectHandFov.description"
        category = BEHAVIOUR
    }

    var retainZoomSteps by boolean(false) {
        name = "zoomify.gui.retainZoomSteps.name"
        description = "zoomify.gui.retainZoomSteps.description"
        category = BEHAVIOUR
    }

    var scrollZoom by boolean(true) {
        name = "zoomify.gui.scrollZoom.name"
        description = "zoomify.gui.scrollZoom.description"
        category = SCROLLING
    }

    var scrollZoomAmount by int(3) {
        name = "zoomify.gui.scrollZoomAmount.name"
        description = "zoomify.gui.scrollZoomAmount.description"
        category = SCROLLING
        range = 1..10
        migrator { type ->
            if (!type.primitive.isInt) {
                PrimitiveType.of(default)
            }
            type
        }
        yaclSliderInterval = 1
    }

    var scrollZoomSmoothness by int(70) {
        name = "zoomify.gui.scrollZoomSmoothness.name"
        description = "zoomify.gui.scrollZoomSmoothness.description"
        category = SCROLLING
        range = 0..100

        yaclValueFormatter = { Text.of("%d%%".format(it)) }
        yaclSliderInterval = 1
    }

    var linearLikeSteps by boolean(true) {
        name = "zoomify.gui.linearLikeSteps.name"
        description = "zoomify.gui.linearLikeSteps.description"
        category = SCROLLING
    }

    var zoomKeyBehaviour by enum(ZoomKeyBehaviour.HOLD) {
        name = "zoomify.gui.zoomKeyBehaviour.name"
        description = "zoomify.gui.zoomKeyBehaviour.description"
        category = CONTROLS
        migrator { type ->
            if (type.primitive.isString) {
                Zoomify.LOGGER.info("Migrating transition type from string to int")
                PrimitiveType.of(ZoomKeyBehaviour.values().find { keyBehaviour ->
                    keyBehaviour.displayName.lowercase()
                        .replace(Regex("\\W+"), "_")
                        .trim { it == '_' || it.isWhitespace() } == type.primitive.string
                }!!.ordinal).also { needsSaving = true }
            } else type
        }
    }

    var keybindScrolling = false
        private set

    private var _keybindScrolling by boolean(false) {
        name = "zoomify.gui.keybindScrolling.name"
        description = "zoomify.gui.keybindScrolling.description"
        category = CONTROLS
        yaclFlags = setOf(OptionFlag.GAME_RESTART)
    }

    var relativeSensitivity by int(100) {
        name = "zoomify.gui.relativeSensitivity.name"
        description = "zoomify.gui.relativeSensitivity.description"
        category = CONTROLS
        range = 0..150

        yaclValueFormatter = { Text.of("%d%%".format(it)) }
        yaclSliderInterval = 1

        migrator { type ->
            if (type.isPrimitive && type.primitive.isBoolean) {
                needsSaving = true
                if (type.primitive.boolean)
                    PrimitiveType.of(100)
                else
                    PrimitiveType.of(0)
            } else {
                type
            }
        }
    }

    var relativeViewBobbing by boolean(true) {
        name = "zoomify.gui.relativeViewBobbing.name"
        description = "zoomify.gui.relativeViewBobbing.description"
        category = CONTROLS
    }

    var cinematicCamera by int(0) {
        name = "zoomify.gui.cinematicCam.name"
        description = "zoomify.gui.cinematicCam.description"
        category = CONTROLS
        range = 0..250

        migrator { type ->
            if (type.isPrimitive && type.primitive.isBoolean) {
                needsSaving = true
                if (type.primitive.boolean)
                    PrimitiveType.of(100)
                else
                    PrimitiveType.of(0)
            } else {
                type
            }
        }

        yaclValueFormatter = { Text.of("%d%%".format(it)) }
        yaclSliderInterval = 10
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
        for (setting in settings) {
            if (setting is BooleanSetting) setting.yaclUseTickBox = true
        }

        yaclButton({
            Zoomify.unbindConflicting()
        }) {
            name = "zoomify.gui.unbindConflicting.name"
            yaclButtonText = Text.translatable("zoomify.gui.unbindConflicting.button")
            description = "zoomify.gui.unbindConflicting.description"
            category = MISC
        }

        val presetGroup = Group(Text.translatable("zoomify.gui.subcategory.presets"))

        yaclLabel(Text.translatable("zoomify.gui.preset.apply.warning").formatted(Formatting.RED)) {
            yaclGroup = presetGroup
            category = MISC
        }
        for (preset in Presets.values()) {
            yaclButton({
                val client = MinecraftClient.getInstance()
                preset.apply(this)
                client.toastManager.add(SystemToast.create(client, SystemToast.Type.TUTORIAL_HINT, Text.translatable("zoomify.gui.preset.toast.title"), Text.translatable("zoomify.gui.preset.toast.description", Text.translatable(preset.displayName))))

                OptionUtils.forEachOptions(it.config, Option<*>::forgetPendingValue)
                export()
                it.init(client, it.width, it.height)
            }) {
                name = preset.displayName
                yaclGroup = presetGroup
                category = MISC
                yaclButtonText = Text.translatable("zoomify.gui.preset.apply")
            }
        }
    }

    val firstLaunch = filePath.notExists()

    init {
        import()
        if (needsSaving) {
            export()
            needsSaving = false
        }

        keybindScrolling = _keybindScrolling
    }

    fun gui(parent: Screen? = null): Screen =
        yetAnotherConfigLib(Text.translatable("zoomify.gui.title"), parent)
}
