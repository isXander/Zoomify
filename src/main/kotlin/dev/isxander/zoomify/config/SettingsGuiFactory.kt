package dev.isxander.zoomify.config

import dev.isxander.settxi.Setting
import dev.isxander.settxi.impl.DoubleSetting
import dev.isxander.settxi.impl.IntSetting
import dev.isxander.settxi.impl.SettingDisplayName
import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.*
import dev.isxander.yacl3.api.utils.OptionUtils
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.config.migrator.Migrator
import dev.isxander.zoomify.utils.TransitionType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.toast.SystemToast
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.isAccessible

fun createSettingsGui(parent: Screen? = null): Screen {
    return YetAnotherConfigLib.createBuilder().apply {
        title(Text.translatable("zoomify.gui.title"))
        category(ConfigCategory.createBuilder().apply {
            name(Text.translatable("zoomify.gui.category.behaviour"))

            group(OptionGroup.createBuilder().apply {
                name(Text.translatable("zoomify.gui.group.basic"))

                option(Option.createBuilder<Int>().apply {
                    name(Text.translatable("zoomify.gui.initialZoom.name"))
                    descriptionWithDemo(Text.translatable("zoomify.gui.initialZoom.description"))
                    bindSetting(ZoomifySettings::initialZoom)
                    controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                        range(ZoomifySettings::initialZoom.asSetting<Int, IntSetting>().range!!)
                        step(1)
                        valueFormatter { Text.of("%dx".format(it)) }
                    }}
                }.build())

                option(Option.createBuilder<Double>().apply {
                    name(Text.translatable("zoomify.gui.zoomInTime.name"))
                    descriptionWithDemo(Text.translatable("zoomify.gui.zoomInTime.description"))
                    bindSetting(ZoomifySettings::zoomInTime)
                    controller { opt -> DoubleSliderControllerBuilder.create(opt).apply {
                        range(ZoomifySettings::zoomInTime.asSetting<Double, DoubleSetting>().range!!)
                        step(0.1)
                        formatSeconds()
                    }}
                }.build())

                option(Option.createBuilder<Double>().apply {
                    name(Text.translatable("zoomify.gui.zoomOutTime.name"))
                    descriptionWithDemo(Text.translatable("zoomify.gui.zoomOutTime.description"))
                    bindSetting(ZoomifySettings::zoomOutTime)
                    controller { opt -> DoubleSliderControllerBuilder.create(opt).apply {
                        range(ZoomifySettings::zoomOutTime.asSetting<Double, DoubleSetting>().range!!)
                        step(0.1)
                        formatSeconds()
                    }}
                }.build())

                option(Option.createBuilder<TransitionType>().apply {
                    name(Text.translatable("zoomify.gui.zoomInTransition.name"))
                    descriptionWithDemo(Text.translatable("zoomify.gui.zoomInTransition.description"))
                    bindSetting(ZoomifySettings::zoomInTransition)
                    controller { opt -> EnumControllerBuilder.create(opt).apply {
                        formatSettxiEnum()
                        enumClass(TransitionType::class.java)
                    }}
                }.build())

                option(Option.createBuilder<TransitionType>().apply {
                    name(Text.translatable("zoomify.gui.zoomOutTransition.name"))
                    descriptionWithDemo(Text.translatable("zoomify.gui.zoomOutTransition.description"))
                    bindSetting(ZoomifySettings::zoomOutTransition)
                    controller { opt -> EnumControllerBuilder.create(opt).apply {
                        formatSettxiEnum()
                        enumClass(TransitionType::class.java)
                    }}
                }.build())

                option(Option.createBuilder<Boolean>().apply {
                    name(Text.translatable("zoomify.gui.affectHandFov.name"))
                    descriptionWithDemo(Text.translatable("zoomify.gui.affectHandFov.description"))
                    bindSetting(ZoomifySettings::affectHandFov)
                    controller(TickBoxControllerBuilder::create)
                }.build())

                option(Option.createBuilder<Boolean>().apply {
                    name(Text.translatable("zoomify.gui.retainZoomSteps.name"))
                    descriptionWithDemo(Text.translatable("zoomify.gui.retainZoomSteps.description"))
                    bindSetting(ZoomifySettings::retainZoomSteps)
                    controller(TickBoxControllerBuilder::create)
                }.build())
            }.build())

            group(OptionGroup.createBuilder().apply {
                name(Text.translatable(ZoomifySettings.SCROLLING))

                option(Option.createBuilder<Boolean>().apply {
                    name(Text.translatable("zoomify.gui.scrollZoom.name"))
                    descriptionWithDemo(Text.translatable("zoomify.gui.scrollZoom.description"))
                    bindSetting(ZoomifySettings::scrollZoom)
                    controller(TickBoxControllerBuilder::create)
                }.build())

                option(Option.createBuilder<Int>().apply {
                    name(Text.translatable("zoomify.gui.scrollZoomAmount.name"))
                    descriptionWithDemo(Text.translatable("zoomify.gui.scrollZoomAmount.description"))
                    bindSetting(ZoomifySettings::scrollZoomAmount)
                    controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                        range(ZoomifySettings::scrollZoomAmount.asSetting<Int, IntSetting>().range!!)
                        step(1)
                    }}
                }.build())

                option(Option.createBuilder<Int>().apply {
                    useSettxiName(ZoomifySettings::scrollZoomSmoothness)
                    descriptionWithDemo(Text.translatable(ZoomifySettings::scrollZoomSmoothness.setting.description))
                    bindSetting(ZoomifySettings::scrollZoomSmoothness)
                    controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                        range(ZoomifySettings::scrollZoomSmoothness.asSetting<Int, IntSetting>().range!!)
                        step(1)
                        valueFormatter {
                            if (it == 0)
                                Text.translatable("zoomify.gui.formatter.instant")
                            else
                                Text.of("%d%%".format(it))
                        }
                    }}
                }.build())

                option(Option.createBuilder<Boolean>().apply {
                    useSettxiName(ZoomifySettings::linearLikeSteps)
                    descriptionWithDemo(Text.translatable(ZoomifySettings::linearLikeSteps.setting.description))
                    bindSetting(ZoomifySettings::linearLikeSteps)
                    controller(TickBoxControllerBuilder::create)
                }.build())
            }.build())

            group(OptionGroup.createBuilder().apply {
                name(Text.translatable(ZoomifySettings.SPYGLASS))

                option(Option.createBuilder<SpyglassBehaviour>().apply {
                    useSettxiName(ZoomifySettings::spyglassBehaviour)
                    descriptionWithDemo(Text.translatable(ZoomifySettings::spyglassBehaviour.setting.description))
                    bindSetting(ZoomifySettings::spyglassBehaviour)
                    controller { opt -> EnumControllerBuilder.create(opt).apply {
                        enumClass(SpyglassBehaviour::class.java)
                        formatSettxiEnum()
                    }}
                }.build())

                option(Option.createBuilder<OverlayVisibility>().apply {
                    useSettxiName(ZoomifySettings::spyglassOverlayVisibility)
                    descriptionWithDemo(Text.translatable(ZoomifySettings::spyglassOverlayVisibility.setting.description))
                    bindSetting(ZoomifySettings::spyglassOverlayVisibility)
                    controller { opt -> EnumControllerBuilder.create(opt).apply {
                        enumClass(OverlayVisibility::class.java)
                        formatSettxiEnum()
                    }}
                }.build())

                option(Option.createBuilder<SoundBehaviour>().apply {
                    useSettxiName(ZoomifySettings::spyglassSoundBehaviour)
                    descriptionWithDemo(Text.translatable(ZoomifySettings::spyglassSoundBehaviour.setting.description))
                    bindSetting(ZoomifySettings::spyglassSoundBehaviour)
                    controller { opt -> EnumControllerBuilder.create(opt).apply {
                        enumClass(SoundBehaviour::class.java)
                        formatSettxiEnum()
                    }}
                }.build())
            }.build())
        }.build())

        category(ConfigCategory.createBuilder().apply {
            name(Text.translatable(ZoomifySettings.CONTROLS))

            option(Option.createBuilder<ZoomKeyBehaviour>().apply {
                useSettxiName(ZoomifySettings::zoomKeyBehaviour)
                descriptionWithDemo(Text.translatable(ZoomifySettings::zoomKeyBehaviour.setting.description))
                bindSetting(ZoomifySettings::zoomKeyBehaviour)
                controller { opt -> EnumControllerBuilder.create(opt).apply {
                    enumClass(ZoomKeyBehaviour::class.java)
                    formatSettxiEnum()
                }}
            }.build())

            option(Option.createBuilder<Boolean>().apply {
                useSettxiName(ZoomifySettings::_keybindScrolling)
                descriptionWithDemo(Text.translatable(ZoomifySettings::_keybindScrolling.setting.description))
                bindSetting(ZoomifySettings::_keybindScrolling)
                controller(TickBoxControllerBuilder::create)
                flag(OptionFlag.GAME_RESTART)
            }.build())

            option(Option.createBuilder<Int>().apply {
                useSettxiName(ZoomifySettings::relativeSensitivity)
                descriptionWithDemo(Text.translatable(ZoomifySettings::relativeSensitivity.setting.description))
                bindSetting(ZoomifySettings::relativeSensitivity)
                controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                    range(ZoomifySettings::relativeSensitivity.asSetting<Int, IntSetting>().range!!)
                    step(10)
                    valueFormatter {
                        if (it == 0)
                            ScreenTexts.OFF
                        else
                            Text.of("%d%%".format(it))
                    }
                }}
            }.build())

            option(Option.createBuilder<Boolean>().apply {
                useSettxiName(ZoomifySettings::relativeViewBobbing)
                descriptionWithDemo(Text.translatable(ZoomifySettings::relativeViewBobbing.setting.description))
                bindSetting(ZoomifySettings::relativeViewBobbing)
                controller(TickBoxControllerBuilder::create)
            }.build())

            option(Option.createBuilder<Int>().apply {
                useSettxiName(ZoomifySettings::cinematicCamera)
                descriptionWithDemo(Text.translatable(ZoomifySettings::cinematicCamera.setting.description))
                bindSetting(ZoomifySettings::cinematicCamera)
                controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                    range(ZoomifySettings::cinematicCamera.asSetting<Int, IntSetting>().range!!)
                    step(10)
                    valueFormatter {
                        if (it == 0)
                            ScreenTexts.OFF
                        else
                            Text.of("%d%%".format(it))
                    }
                }}
            }.build())
        }.build())

        category(ConfigCategory.createBuilder().apply {
            name(Text.translatable(ZoomifySettings.SECONDARY))

            option(LabelOption.create(Text.translatable("zoomify.gui.secondaryZoom.label")))

            option(Option.createBuilder<Int>().apply {
                useSettxiName(ZoomifySettings::secondaryZoomAmount)
                descriptionWithDemo(Text.translatable(ZoomifySettings::secondaryZoomAmount.setting.description))
                bindSetting(ZoomifySettings::secondaryZoomAmount)
                controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                    range(ZoomifySettings::secondaryZoomAmount.asSetting<Int, IntSetting>().range!!)
                    step(1)
                    valueFormatter { Text.of("%dx".format(it)) }
                }}
            }.build())

            option(Option.createBuilder<Double>().apply {
                useSettxiName(ZoomifySettings::secondaryZoomInTime)
                descriptionWithDemo(Text.translatable(ZoomifySettings::secondaryZoomInTime.setting.description))
                bindSetting(ZoomifySettings::secondaryZoomInTime)
                controller { opt -> DoubleSliderControllerBuilder.create(opt).apply {
                    range(ZoomifySettings::secondaryZoomInTime.asSetting<Double, DoubleSetting>().range!!)
                    step(2.0)
                    valueFormatter { Text.translatable("zoomify.gui.formatter.seconds", "%.0f".format(it)) }
                }}
            }.build())

            option(Option.createBuilder<Double>().apply {
                useSettxiName(ZoomifySettings::secondaryZoomOutTime)
                descriptionWithDemo(Text.translatable(ZoomifySettings::secondaryZoomOutTime.setting.description))
                bindSetting(ZoomifySettings::secondaryZoomOutTime)
                controller { opt -> DoubleSliderControllerBuilder.create(opt).apply {
                    range(ZoomifySettings::secondaryZoomOutTime.asSetting<Double, DoubleSetting>().range!!)
                    step(0.25)
                    valueFormatter {
                        if (it == 0.0)
                            Text.translatable("zoomify.gui.formatter.instant")
                        else
                            Text.translatable("zoomify.gui.formatter.seconds", "%.2f".format(it))
                    }
                }}
            }.build())

            option(Option.createBuilder<Boolean>().apply {
                useSettxiName(ZoomifySettings::secondaryHideHUDOnZoom)
                descriptionWithDemo(Text.translatable(ZoomifySettings::secondaryHideHUDOnZoom.setting.description))
                bindSetting(ZoomifySettings::secondaryHideHUDOnZoom)
                controller(TickBoxControllerBuilder::create)
            }.build())
        }.build())

        category(ConfigCategory.createBuilder().apply {
            name(Text.translatable(ZoomifySettings.MISC))

            option(ButtonOption.createBuilder().apply {
                name(Text.translatable("zoomify.gui.unbindConflicting.name"))
                description(OptionDescription.of(Text.translatable("zoomify.gui.unbindConflicting.description")))

                action { _, _ -> Zoomify.unbindConflicting() }
            }.build())

            option(ButtonOption.createBuilder().apply {
                name(Text.translatable("zoomify.gui.checkMigrations.name"))
                description(OptionDescription.of(Text.translatable("zoomify.gui.checkMigrations.description")))

                action { _, _ ->
                    if (!Migrator.checkMigrations()) {
                        val client = MinecraftClient.getInstance()
                        client.toastManager.add(SystemToast.create(
                            client,
                            SystemToast.Type.TUTORIAL_HINT,
                            Text.translatable("zoomify.gui.title"),
                            Text.translatable("zoomify.migrate.no_migrations")
                        ))
                    }
                }
            }.build())

            group(OptionGroup.createBuilder().apply {
                name(Text.translatable("zoomify.gui.subcategory.presets"))

                option(LabelOption.create(Text.translatable("zoomify.gui.preset.apply.warning").formatted(Formatting.RED)))

                for (preset in Presets.values()) {
                    option(ButtonOption.createBuilder().apply {
                        name(Text.translatable(preset.displayName))

                        action { screen, _ ->
                            val client = MinecraftClient.getInstance()
                            preset.apply(ZoomifySettings)
                            client.toastManager.add(SystemToast.create(client, SystemToast.Type.TUTORIAL_HINT, Text.translatable("zoomify.gui.preset.toast.title"), Text.translatable("zoomify.gui.preset.toast.description", Text.translatable(preset.displayName))))

                            OptionUtils.forEachOptions(screen.config, Option<*>::forgetPendingValue)
                            ZoomifySettings.export()
                            screen.init(client, screen.width, screen.height)
                        }
                    }.build())
                }
            }.build())
        }.build())

        save(ZoomifySettings::export)
    }.build().generateScreen(parent)
}

private fun Option.Builder<*>.useSettxiName(prop: KMutableProperty0<*>) {
    name(Text.translatable(prop.setting.name))
}

private fun Option.Builder<*>.descriptionWithDemo(text: Text) {
    description(OptionDescription.createBuilder().apply {
        text(text)
//        customImage(CompletableFuture.completedFuture(Optional.of(ZoomDemoImageRenderer(
//            ZoomHelper(
//                TransitionInterpolator(
//                    { TransitionType.EASE_OUT_EXP },
//                    { TransitionType.EASE_OUT_EXP },
//                    { 2.0 },
//                    { 0.5 },
//                ),
//                InstantInterpolator,
//                { 4 },
//                { 4 },
//                { 5 },
//                { true }
//            )
//        ))))
    }.build())
}

private fun <T : Any> Option.Builder<T>.bindSetting(setting: Setting<T>) {
    binding(setting.default, { setting.get(false) }, setting::set)
}

private val <T> KMutableProperty0<T>.setting: Setting<T>
    get() = this.asSetting()
private fun <I, O : Setting<I>> KMutableProperty0<I>.asSetting(): O {
    isAccessible = true
    return getDelegate() as O
}

private fun <T : Any> Option.Builder<T>.bindSetting(settingProp: KMutableProperty0<T>) {
    settingProp.isAccessible = true
    bindSetting(settingProp.getDelegate() as Setting<T>)
}

private fun <T> SliderControllerBuilder<T, *>.range(range: ClosedRange<T>) where T : Number, T : Comparable<T> {
    range(range.start, range.endInclusive)
}

private fun ValueFormattableController<*, *>.formatSeconds() {
    valueFormatter { Text.translatable("zoomify.gui.formatter.seconds", "%.1f".format(it)) }
}

private fun <T> EnumControllerBuilder<T>.formatSettxiEnum() where T : Enum<T>, T : SettingDisplayName {
    valueFormatter { Text.translatable(it.displayName) }
}
