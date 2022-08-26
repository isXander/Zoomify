package dev.isxander.zoomify.config

import dev.isxander.settxi.gui.clothGui
import dev.isxander.settxi.impl.*
import dev.isxander.settxi.serialization.PrimitiveType
import dev.isxander.settxi.serialization.SettxiConfigKotlinx
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.config.gui.ButtonEntryBuilder
import dev.isxander.zoomify.utils.TransitionType
import me.shedaniel.clothconfig2.api.AbstractConfigEntry
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.gui.ClothConfigScreen
import me.shedaniel.clothconfig2.gui.GlobalizedClothConfigScreen
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import net.minecraft.util.Util
import kotlin.io.path.notExists


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
    }

    var scrollZoomSmoothness by int(70) {
        name = "zoomify.gui.scrollZoomSmoothness.name"
        description = "zoomify.gui.scrollZoomSmoothness.description"
        category = SCROLLING
        range = 0..100
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

    var relativeSensitivity by int(100) {
        name = "zoomify.gui.relativeSensitivity.name"
        description = "zoomify.gui.relativeSensitivity.description"
        category = CONTROLS
        range = 0..150
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

    val firstLaunch = filePath.notExists()

    init {
        import()
        if (needsSaving) {
            export()
            needsSaving = false
        }
    }

    fun gui(parent: Screen? = null): Screen =
        clothGui(Text.translatable("zoomify.gui.title"), parent) {
            val category = this.getOrCreateCategory(Text.translatable("zoomify.gui.category.misc"))

            category.addEntry(ButtonEntryBuilder(
                Text.translatable("zoomify.gui.unbindConflicting.name"),
                Text.translatable("zoomify.gui.unbindConflicting.button")
            ) {
                Zoomify.unbindConflicting()
            }.apply {
                setTooltip(Text.translatable("zoomify.gui.unbindConflicting.description"))
            }.build())

            val presetsSubCategory = entryBuilder().startSubCategory(Text.translatable("zoomify.gui.subcategory.presets"))
            presetsSubCategory.setExpanded(true)
            for (preset in Presets.values()) {
                presetsSubCategory.add(
                    ButtonEntryBuilder(
                        Text.translatable(preset.displayName),
                        Text.translatable("zoomify.gui.preset.apply")
                    ) {
                        preset.apply(ZoomifySettings)
                        export()
                        MinecraftClient.getInstance().setScreen(gui(parent))
                    }.apply {
                        setTooltip(
                            Text.translatable(
                                "zoomify.gui.preset.apply.description",
                                Text.translatable(preset.displayName)
                            ), Text.translatable("zoomify.gui.preset.apply.warning").formatted(Formatting.RED)
                        )
                    }.build()
                )
            }
            category.addEntry(presetsSubCategory.build())

            setAfterInitConsumer { screen ->
                val text = Text.translatable("zoomify.gui.donate")
                val width = MinecraftClient.getInstance().textRenderer.getWidth(text) + 8
                Screens.getButtons(screen).add(ButtonWidget(screen.width - width - 4, 4, width, 20, text) {
                    Util.getOperatingSystem().open("https://ko-fi.com/isxander")
                })
            }
        }
}
