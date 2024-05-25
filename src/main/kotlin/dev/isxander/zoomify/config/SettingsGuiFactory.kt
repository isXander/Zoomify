package dev.isxander.zoomify.config

import dev.isxander.settxi.Setting
import dev.isxander.settxi.impl.DoubleSetting
import dev.isxander.settxi.impl.IntSetting
import dev.isxander.settxi.impl.SettingDisplayName
import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.*
import dev.isxander.yacl3.api.utils.OptionUtils
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.config.demo.ControlEmulation
import dev.isxander.zoomify.config.demo.FirstPersonDemo
import dev.isxander.zoomify.config.demo.ThirdPersonDemo
import dev.isxander.zoomify.config.demo.ZoomDemoImageRenderer
import dev.isxander.zoomify.config.migrator.Migrator
import dev.isxander.zoomify.utils.TransitionType
import dev.isxander.zoomify.utils.toast
import dev.isxander.zoomify.zoom.*
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.isAccessible

fun createSettingsGui(parent: Screen? = null): Screen {
    var inTransition = ZoomifySettings.zoomInTransition
    var outTransition = ZoomifySettings.zoomOutTransition
    var inDuration = ZoomifySettings.zoomInTime
    var outDuration = ZoomifySettings.zoomOutTime
    var initialZoomAmt = ZoomifySettings.initialZoom
    var scrollZoomAmt = ZoomifySettings.scrollZoomAmount
    var linearLikeSteps = ZoomifySettings.linearLikeSteps
    var scrollZoomSmoothness = ZoomifySettings.scrollZoomSmoothness
    var canScrollZoom = ZoomifySettings.scrollZoom
    var secondaryZoomInTime = ZoomifySettings.secondaryZoomInTime
    var secondaryZoomOutTime = ZoomifySettings.secondaryZoomOutTime
    var secondaryZoomAmount = ZoomifySettings.secondaryZoomAmount

    val zoomHelperFactory = { ZoomHelper(
        TransitionInterpolator(
            { inTransition },
            { outTransition },
            { inDuration },
            { outDuration },
        ),
        SmoothInterpolator {
            Mth.lerp(
                scrollZoomSmoothness / 100.0,
                1.0,
                0.1
            )
        },
        { initialZoomAmt },
        { scrollZoomAmt },
        { if (canScrollZoom) 10 else 0 },
        { linearLikeSteps }
    )}
    val initialOnlyDemo = FirstPersonDemo(zoomHelperFactory(), ControlEmulation.InitialOnly).also {
        it.keepHandFov = !ZoomifySettings.affectHandFov
    }
    val scrollOnlyDemo = FirstPersonDemo(zoomHelperFactory(), ControlEmulation.ScrollOnly).also {
        it.keepHandFov = !ZoomifySettings.affectHandFov
    }
    val secondaryZoomDemo = ThirdPersonDemo(
        ZoomHelper(
            TimedInterpolator({ secondaryZoomInTime }, { secondaryZoomOutTime }),
            InstantInterpolator,
            initialZoom = { secondaryZoomAmount },
            scrollZoomAmount = { 0 },
            maxScrollTiers = { 0 },
            linearLikeSteps = { false },
        ),
        ControlEmulation.InitialOnly
    ).also {
        it.renderHud = !ZoomifySettings.secondaryHideHUDOnZoom
    }

    fun <T> Option.Builder<T>.updateDemo(updateFunc: (T, ZoomDemoImageRenderer) -> Unit) {
        listener { opt, v ->
            updateFunc(v, initialOnlyDemo)
            updateFunc(v, scrollOnlyDemo)
            updateFunc(v, secondaryZoomDemo)
            initialOnlyDemo.pause()
            scrollOnlyDemo.pause()
            secondaryZoomDemo.pause()
        }
    }

    val screen = YetAnotherConfigLib.createBuilder().apply {
        title(Component.translatable("zoomify.gui.title"))
        category(ConfigCategory.createBuilder().apply {
            name(Component.translatable("zoomify.gui.category.behaviour"))

            group(OptionGroup.createBuilder().apply {
                name(Component.translatable("zoomify.gui.group.basic"))

                option(Option.createBuilder<Int>().apply {
                    name(Component.translatable("zoomify.gui.initialZoom.name"))
                    desc {
                        text(Component.translatable("zoomify.gui.initialZoom.description"))
                        demo(initialOnlyDemo)
                    }
                    bindSetting(ZoomifySettings::initialZoom)
                    controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                        range(ZoomifySettings::initialZoom.asSetting<Int, IntSetting>().range!!)
                        step(1)
                        formatValue { Component.literal("%dx".format(it)) }
                    }}
                    updateDemo { v, _ -> initialZoomAmt = v }
                }.build())

                option(Option.createBuilder<Double>().apply {
                    name(Component.translatable("zoomify.gui.zoomInTime.name"))
                    desc {
                        text(Component.translatable("zoomify.gui.zoomInTime.description"))
                        demo(initialOnlyDemo)
                    }
                    bindSetting(ZoomifySettings::zoomInTime)
                    controller { opt -> DoubleSliderControllerBuilder.create(opt).apply {
                        range(ZoomifySettings::zoomInTime.asSetting<Double, DoubleSetting>().range!!)
                        step(0.1)
                        formatSeconds()
                    }}
                    updateDemo { v, _ -> inDuration = v }
                }.build())

                option(Option.createBuilder<Double>().apply {
                    name(Component.translatable("zoomify.gui.zoomOutTime.name"))
                    desc {
                        text(Component.translatable("zoomify.gui.zoomOutTime.description"))
                        demo(initialOnlyDemo)
                    }
                    bindSetting(ZoomifySettings::zoomOutTime)
                    controller { opt -> DoubleSliderControllerBuilder.create(opt).apply {
                        range(ZoomifySettings::zoomOutTime.asSetting<Double, DoubleSetting>().range!!)
                        step(0.1)
                        formatSeconds()
                    }}
                    updateDemo { v, _ -> outDuration = v }
                }.build())

                option(Option.createBuilder<TransitionType>().apply {
                    name(Component.translatable("zoomify.gui.zoomInTransition.name"))
                    desc {
                        text(Component.translatable("zoomify.gui.zoomInTransition.description"))
                        demo(initialOnlyDemo)
                    }
                    bindSetting(ZoomifySettings::zoomInTransition)
                    controller { opt -> EnumControllerBuilder.create(opt).apply {
                        formatSettxiEnum()
                        enumClass(TransitionType::class.java)
                    }}
                    updateDemo { v, _ -> inTransition = v }
                }.build())

                option(Option.createBuilder<TransitionType>().apply {
                    name(Component.translatable("zoomify.gui.zoomOutTransition.name"))
                    desc {
                        text(Component.translatable("zoomify.gui.zoomOutTransition.description"))
                        demo(initialOnlyDemo)
                    }
                    bindSetting(ZoomifySettings::zoomOutTransition)
                    controller { opt -> EnumControllerBuilder.create(opt).apply {
                        formatSettxiEnum()
                        enumClass(TransitionType::class.java)
                    }}
                    updateDemo { v, _ -> outTransition = v.opposite() }
                }.build())

                option(Option.createBuilder<Boolean>().apply {
                    name(Component.translatable("zoomify.gui.affectHandFov.name"))
                    desc {
                        text(Component.translatable("zoomify.gui.affectHandFov.description"))
                        demo(scrollOnlyDemo)
                    }
                    bindSetting(ZoomifySettings::affectHandFov)
                    controller(TickBoxControllerBuilder::create)
                    updateDemo { v, demo -> (demo as? FirstPersonDemo)?.keepHandFov = !v }
                }.build())
            }.build())

            group(OptionGroup.createBuilder().apply {
                name(Component.translatable(ZoomifySettings.SCROLLING))

                val scrollOpts = mutableListOf<Option<*>>()

                option(Option.createBuilder<Boolean>().apply {
                    name(Component.translatable("zoomify.gui.scrollZoom.name"))
                    desc {
                        text(Component.translatable("zoomify.gui.scrollZoom.description"))
                        demo(scrollOnlyDemo)
                    }
                    bindSetting(ZoomifySettings::scrollZoom)
                    controller(TickBoxControllerBuilder::create)
                    updateDemo { v, _ -> canScrollZoom = v }
                    listener { _, v -> scrollOpts.forEach { it.setAvailable(v) } }
                }.build())

                option(Option.createBuilder<Int>().apply {
                    name(Component.translatable("zoomify.gui.scrollZoomAmount.name"))
                    desc {
                        text(Component.translatable("zoomify.gui.scrollZoomAmount.description"))
                        demo(scrollOnlyDemo)
                    }
                    bindSetting(ZoomifySettings::scrollZoomAmount)
                    controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                        range(ZoomifySettings::scrollZoomAmount.asSetting<Int, IntSetting>().range!!)
                        step(1)
                    }}
                    updateDemo { v, _ -> scrollZoomAmt = v }
                }.build().also { scrollOpts.add(it) })

                option(Option.createBuilder<Int>().apply {
                    name(Component.translatable("zoomify.gui.scrollZoomSmoothness.name"))
                    desc {
                        text(Component.translatable("zoomify.gui.scrollZoomSmoothness.description"))
                        demo(scrollOnlyDemo)
                    }
                    bindSetting(ZoomifySettings::scrollZoomSmoothness)
                    controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                        range(ZoomifySettings::scrollZoomSmoothness.asSetting<Int, IntSetting>().range!!)
                        step(1)
                        valueFormatter {
                            if (it == 0)
                                Component.translatable("zoomify.gui.formatter.instant")
                            else
                                Component.literal("%d%%".format(it))
                        }
                    }}
                    updateDemo { v, _ -> scrollZoomSmoothness = v }
                }.build().also { scrollOpts.add(it) })

                option(Option.createBuilder<Boolean>().apply {
                    name(Component.translatable("zoomify.gui.linearLikeSteps.name"))
                    desc {
                        text(Component.translatable("zoomify.gui.linearLikeSteps.description"))
                        demo(scrollOnlyDemo)
                    }
                    bindSetting(ZoomifySettings::linearLikeSteps)
                    controller(TickBoxControllerBuilder::create)
                    updateDemo { v, _ -> linearLikeSteps = v }
                }.build().also { scrollOpts.add(it) })

                option(Option.createBuilder<Boolean>().apply {
                    name(Component.translatable("zoomify.gui.retainZoomSteps.name"))
                    desc {
                        text(Component.translatable("zoomify.gui.retainZoomSteps.description"))
                    }
                    bindSetting(ZoomifySettings::retainZoomSteps)
                    controller(TickBoxControllerBuilder::create)
                }.build().also { scrollOpts.add(it) })
            }.build())

            group(OptionGroup.createBuilder().apply {
                name(Component.translatable(ZoomifySettings.SPYGLASS))

                option(Option.createBuilder<SpyglassBehaviour>().apply {
                    useSettxiName(ZoomifySettings::spyglassBehaviour)
                    description(OptionDescription.of(Component.translatable(ZoomifySettings::spyglassBehaviour.setting.description)))
                    bindSetting(ZoomifySettings::spyglassBehaviour)
                    controller { opt -> EnumControllerBuilder.create(opt).apply {
                        enumClass(SpyglassBehaviour::class.java)
                        formatSettxiEnum()
                    }}
                }.build())

                option(Option.createBuilder<OverlayVisibility>().apply {
                    useSettxiName(ZoomifySettings::spyglassOverlayVisibility)
                    desc {
                        text(Component.translatable(ZoomifySettings::spyglassOverlayVisibility.setting.description))
                    }
                    bindSetting(ZoomifySettings::spyglassOverlayVisibility)
                    controller { opt -> EnumControllerBuilder.create(opt).apply {
                        enumClass(OverlayVisibility::class.java)
                        formatSettxiEnum()
                    }}
                }.build())

                option(Option.createBuilder<SoundBehaviour>().apply {
                    useSettxiName(ZoomifySettings::spyglassSoundBehaviour)
                    desc {
                        text(Component.translatable(ZoomifySettings::spyglassSoundBehaviour.setting.description))
                    }
                    bindSetting(ZoomifySettings::spyglassSoundBehaviour)
                    controller { opt -> EnumControllerBuilder.create(opt).apply {
                        enumClass(SoundBehaviour::class.java)
                        formatSettxiEnum()
                    }}
                }.build())
            }.build())
        }.build())

        category(ConfigCategory.createBuilder().apply {
            name(Component.translatable(ZoomifySettings.CONTROLS))

            option(Option.createBuilder<Boolean>().apply {
                useSettxiName(ZoomifySettings::_keybindScrolling)
                desc {
                    text(Component.translatable(ZoomifySettings::_keybindScrolling.setting.description))
                }
                bindSetting(ZoomifySettings::_keybindScrolling)
                controller(TickBoxControllerBuilder::create)
                flag(OptionFlag.GAME_RESTART)
            }.build())

            option(Option.createBuilder<Int>().apply {
                useSettxiName(ZoomifySettings::relativeSensitivity)
                desc {
                    text(Component.translatable(ZoomifySettings::relativeSensitivity.setting.description))
                }
                bindSetting(ZoomifySettings::relativeSensitivity)
                controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                    range(ZoomifySettings::relativeSensitivity.asSetting<Int, IntSetting>().range!!)
                    step(10)
                    valueFormatter {
                        if (it == 0)
                            CommonComponents.OPTION_OFF
                        else
                            Component.literal("%d%%".format(it))
                    }
                }}
            }.build())

            option(Option.createBuilder<Boolean>().apply {
                useSettxiName(ZoomifySettings::relativeViewBobbing)
                desc {
                    text(Component.translatable(ZoomifySettings::relativeViewBobbing.setting.description))
                }
                bindSetting(ZoomifySettings::relativeViewBobbing)
                controller(TickBoxControllerBuilder::create)
            }.build())

            option(Option.createBuilder<Int>().apply {
                useSettxiName(ZoomifySettings::cinematicCamera)
                desc {
                    text(Component.translatable(ZoomifySettings::cinematicCamera.setting.description))
                }
                bindSetting(ZoomifySettings::cinematicCamera)
                controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                    range(ZoomifySettings::cinematicCamera.asSetting<Int, IntSetting>().range!!)
                    step(10)
                    valueFormatter {
                        if (it == 0)
                            CommonComponents.OPTION_OFF
                        else
                            Component.literal("%d%%".format(it))
                    }
                }}
            }.build())
        }.build())

        category(ConfigCategory.createBuilder().apply {
            name(Component.translatable(ZoomifySettings.SECONDARY))

            option(LabelOption.create(Component.translatable("zoomify.gui.secondaryZoom.label")))

            option(Option.createBuilder<Int>().apply {
                useSettxiName(ZoomifySettings::secondaryZoomAmount)
                desc {
                    text(Component.translatable(ZoomifySettings::secondaryZoomAmount.setting.description))
                    demo(secondaryZoomDemo)
                }
                bindSetting(ZoomifySettings::secondaryZoomAmount)
                controller { opt -> IntegerSliderControllerBuilder.create(opt).apply {
                    range(ZoomifySettings::secondaryZoomAmount.asSetting<Int, IntSetting>().range!!)
                    step(1)
                    valueFormatter { Component.literal("%dx".format(it)) }
                }}
                updateDemo { v, _ -> secondaryZoomAmount = v }
            }.build())

            option(Option.createBuilder<Double>().apply {
                useSettxiName(ZoomifySettings::secondaryZoomInTime)
                desc {
                    text(Component.translatable(ZoomifySettings::secondaryZoomInTime.setting.description))
                    demo(secondaryZoomDemo)
                }
                bindSetting(ZoomifySettings::secondaryZoomInTime)
                controller { opt -> DoubleSliderControllerBuilder.create(opt).apply {
                    range(ZoomifySettings::secondaryZoomInTime.asSetting<Double, DoubleSetting>().range!!)
                    step(2.0)
                    valueFormatter { Component.translatable("zoomify.gui.formatter.seconds", "%.0f".format(it)) }
                }}
                updateDemo { v, _ -> secondaryZoomInTime = v }
            }.build())

            option(Option.createBuilder<Double>().apply {
                useSettxiName(ZoomifySettings::secondaryZoomOutTime)
                desc {
                    text(Component.translatable(ZoomifySettings::secondaryZoomOutTime.setting.description))
                    demo(secondaryZoomDemo)
                }
                bindSetting(ZoomifySettings::secondaryZoomOutTime)
                controller { opt -> DoubleSliderControllerBuilder.create(opt).apply {
                    range(ZoomifySettings::secondaryZoomOutTime.asSetting<Double, DoubleSetting>().range!!)
                    step(0.25)
                    valueFormatter {
                        if (it == 0.0)
                            Component.translatable("zoomify.gui.formatter.instant")
                        else
                            Component.translatable("zoomify.gui.formatter.seconds", "%.2f".format(it))
                    }
                }}
                updateDemo { v, _ -> secondaryZoomOutTime = v }
            }.build())

            option(Option.createBuilder<Boolean>().apply {
                useSettxiName(ZoomifySettings::secondaryHideHUDOnZoom)
                desc {
                    text(Component.translatable(ZoomifySettings::secondaryHideHUDOnZoom.setting.description))
                    demo(secondaryZoomDemo)
                }
                bindSetting(ZoomifySettings::secondaryHideHUDOnZoom)
                controller(TickBoxControllerBuilder::create)
                updateDemo { v, demo -> (demo as? ThirdPersonDemo)?.renderHud = !v }
            }.build())
        }.build())

        category(ConfigCategory.createBuilder().apply {
            name(Component.translatable(ZoomifySettings.MISC))

            option(ButtonOption.createBuilder().apply {
                name(Component.translatable("zoomify.gui.unbindConflicting.name"))
                description(OptionDescription.of(Component.translatable("zoomify.gui.unbindConflicting.description")))

                action { _, _ -> Zoomify.unbindConflicting() }
            }.build())

            option(ButtonOption.createBuilder().apply {
                name(Component.translatable("zoomify.gui.checkMigrations.name"))
                description(OptionDescription.of(Component.translatable("zoomify.gui.checkMigrations.description")))

                action { _, _ ->
                    if (!Migrator.checkMigrations()) {
                        toast(
                            Component.translatable("zoomify.gui.title"),
                            Component.translatable("zoomify.migrate.no_migrations")
                        )
                    }
                }
            }.build())

            group(OptionGroup.createBuilder().apply {
                name(Component.translatable("zoomify.gui.subcategory.presets"))

                option(LabelOption.create(Component.translatable("zoomify.gui.preset.apply.warning").withStyle(ChatFormatting.RED)))

                for (preset in Presets.entries) {
                    option(ButtonOption.createBuilder().apply {
                        name(Component.translatable(preset.displayName))

                        action { screen, _ ->
                            val minecraft = Minecraft.getInstance()
                            preset.apply(ZoomifySettings)

                            toast(
                                Component.translatable("zoomify.gui.preset.toast.title"),
                                Component.translatable("zoomify.gui.preset.toast.description", Component.translatable(preset.displayName))
                            )

                            OptionUtils.forEachOptions(screen.config, Option<*>::forgetPendingValue)
                            ZoomifySettings.export()
                            screen.init(minecraft, screen.width, screen.height)
                        }
                    }.build())
                }
            }.build())
        }.build())

        save(ZoomifySettings::export)
    }.build().generateScreen(parent)

    return screen
}

private fun <T> Option.Builder<T>.desc(descriptionFunc: OptionDescription.Builder.(T) -> Unit) {
    description { v -> OptionDescription.createBuilder().also { descriptionFunc(it, v) }.build() }
}

private fun OptionDescription.Builder.demo(demo: ZoomDemoImageRenderer) {
    customImage(CompletableFuture.completedFuture(Optional.of(demo)))
}

private fun Option.Builder<*>.useSettxiName(prop: KMutableProperty0<*>) {
    name(Component.translatable(prop.setting.name))
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
    formatValue { Component.translatable("zoomify.gui.formatter.seconds", "%.1f".format(it)) }
}

private fun <T> EnumControllerBuilder<T>.formatSettxiEnum() where T : Enum<T>, T : SettingDisplayName {
    formatValue { Component.translatable(it.displayName) }
}
