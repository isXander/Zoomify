package dev.isxander.zoomify.config

import dev.isxander.settxi.gui.spruce.*
import dev.isxander.settxi.impl.*
import dev.isxander.settxi.serialization.PrimitiveType
import dev.isxander.settxi.serialization.SettxiFileConfig
import dev.isxander.settxi.serialization.kotlinxSerializer
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.config.settxi.button
import dev.isxander.zoomify.utils.TransitionType
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Util
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
        range = 1..20
        spruceUITextGetter = { Text.literal("%dx".format(it)) }
    }

    var zoomInTime by double(1.0) {
        name = "zoomify.gui.zoomInTime.name"
        description = "zoomify.gui.zoomInTime.description"
        category = BEHAVIOUR
        range = 0.1..5.0
        spruceUIHalfWidth = true
        spruceUITextGetter = { Text.literal("%.2f secs".format(it)) }
        spruceUISliderStep = 0.05
    }

    var zoomOutTime by double(0.5) {
        name = "zoomify.gui.zoomOutTime.name"
        description = "zoomify.gui.zoomOutTime.description"
        category = BEHAVIOUR
        range = 0.1..5.0
        spruceUIHalfWidth = true
        spruceUITextGetter = { Text.literal("%.2f secs".format(it)) }
        spruceUISliderStep = 0.05
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
        spruceUIHalfWidth = true
    }

    var retainZoomSteps by boolean(false) {
        name = "zoomify.gui.retainZoomSteps.name"
        description = "zoomify.gui.retainZoomSteps.description"
        category = BEHAVIOUR
        spruceUIHalfWidth = true
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
        spruceUIHalfWidth = true
    }

    var linearLikeSteps by boolean(true) {
        name = "zoomify.gui.linearLikeSteps.name"
        description = "zoomify.gui.linearLikeSteps.description"
        category = SCROLLING
        spruceUIHalfWidth = true
    }

    var scrollZoomSmoothness by int(70) {
        name = "zoomify.gui.scrollZoomSmoothness.name"
        description = "zoomify.gui.scrollZoomSmoothness.description"
        category = SCROLLING
        range = 0..100

        spruceUITextGetter = { Text.literal("%d%%".format(it)) }
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

    var relativeSensitivity by int(100) {
        name = "zoomify.gui.relativeSensitivity.name"
        description = "zoomify.gui.relativeSensitivity.description"
        category = CONTROLS
        range = 0..150
        spruceUITextGetter = { Text.literal("%d%%".format(it)) }
        spruceUIHalfWidth = true
    }

    var relativeViewBobbing by boolean(true) {
        name = "zoomify.gui.relativeViewBobbing.name"
        description = "zoomify.gui.relativeViewBobbing.description"
        category = CONTROLS
        spruceUIHalfWidth = true
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

        spruceUITextGetter = { Text.literal("Value: %d%%".format(it)) }
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

    var presetSelection by enum(Presets.OPTIFINE) {
        name = "zoomify.gui.preset.prefix"
        category = MISC
        shouldSave = false
        spruceUIHalfWidth = true
    }

    var applyPresetButton by button({
        presetSelection.apply(ZoomifySettings)
        presetSelection = Presets.OPTIFINE
        export()

        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen is SettxiSpruceScreen) {
            currentScreen.init(MinecraftClient.getInstance(), currentScreen.width, currentScreen.height)
        }
    }) {
        name = "zoomify.gui.preset.apply"
        description = "zoomify.gui.preset.apply.description"
        category = MISC
        spruceUIHalfWidth = true
    }

    var unbindConflictingButton by button({ Zoomify.unbindConflicting() }) {
        name = "zoomify.gui.unbindConflicting.name"
        description = "zoomify.gui.unbindConflicting.description"
        category = MISC
    }

    val firstLaunch = filePath.notExists()

    init {
        import()
        if (needsSaving) {
            export()
            needsSaving = false
        }
    }

    fun gui(parent: Screen? = null): Screen =
        spruceUI(Text.translatable("zoomify.gui.title"), parent) {
            val text = Text.translatable("zoomify.gui.donate")
            val width = MinecraftClient.getInstance().textRenderer.getWidth(text) + 8
            Screens.getButtons(this).add(ButtonWidget(this.width - width - 1, 1, width, 20, text) {
                Util.getOperatingSystem().open("https://ko-fi.com/isxander")
            })
        }
}
