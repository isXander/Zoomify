package dev.isxander.zoomify.config

import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.*
import dev.isxander.yacl3.api.utils.OptionUtils
import dev.isxander.yacl3.config.v3.ConfigEntry
import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.config.v3.value
import dev.isxander.yacl3.dsl.*
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.config.demo.ControlEmulation
import dev.isxander.zoomify.config.demo.FirstPersonDemo
import dev.isxander.zoomify.config.demo.ThirdPersonDemo
import dev.isxander.zoomify.config.demo.ZoomDemoImageRenderer
import dev.isxander.zoomify.config.migrator.Migrator
import dev.isxander.zoomify.utils.toast
import dev.isxander.zoomify.zoom.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import kotlin.reflect.KMutableProperty0

fun createSettingsGui(parent: Screen?) = SettingsGuiFactory().createSettingsGui(parent)

private class SettingsGuiFactory {
    val settings = ZoomifySettings(ZoomifySettings)

    val initialOnlyDemo = FirstPersonDemo(RegularZoomHelper(settings), ControlEmulation.InitialOnly).also {
        it.keepHandFov = !settings.affectHandFov.value
    }
    val scrollOnlyDemo = FirstPersonDemo(RegularZoomHelper(settings), ControlEmulation.ScrollOnly).also {
        it.keepHandFov = !settings.affectHandFov.value
    }
    val secondaryZoomDemo = ThirdPersonDemo(
        SecondaryZoomHelper(settings),
        ControlEmulation.InitialOnly
    ).also {
        it.renderHud = !settings.secondaryHideHUDOnZoom.value
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

    fun <T> Option.Builder<T>.updateDemo(prop: KMutableProperty0<T>) {
        updateDemo { v, _ -> prop.set(v) }
    }

    fun <T : Any> OptionRegistrar.registerDemo(entryGetter: (ZoomifySettings) -> ConfigEntry<T>, demo: ZoomDemoImageRenderer, block: OptionDsl<T>.() -> Unit): Option<T> {
        val globalSettingsEntry = entryGetter(ZoomifySettings)
        val demoSettingsEntry = entryGetter(settings)
        return this.register(globalSettingsEntry) {
            descriptionBuilder {
                addDefaultText()
                customImage(demo)
            }

            updateDemo { v, _ -> demoSettingsEntry.value = v }

            block()
        }
    }

    fun createSettingsGui(parent: Screen?) = YetAnotherConfigLib("zoomify") {
        save(ZoomifySettings::saveToFile)

        val behaviour by categories.registering {
            val basic by groups.registering {
                options.registerDemo(ZoomifySettings::initialZoom, initialOnlyDemo) {
                    controller = slider(range = 1..10, step = 1, formatter = { v: Int ->
                        Component.literal("%dx".format(v))
                    })
                }

                options.registerDemo(ZoomifySettings::zoomInTime, initialOnlyDemo) {
                    controller = slider(range = 0.1..5.0, step = 0.1, formatter = formatSeconds())
                }

                options.registerDemo(ZoomifySettings::zoomOutTime, initialOnlyDemo) {
                    controller = slider(range = 0.1..5.0, step = 0.1, formatter = formatSeconds())
                }

                options.registerDemo(ZoomifySettings::zoomInTransition, initialOnlyDemo) {
                    controller = enumSwitch()
                }

                options.registerDemo(ZoomifySettings::zoomOutTransition, initialOnlyDemo) {
                    controller = enumSwitch()
                }

                options.register(ZoomifySettings.affectHandFov) {
                    descriptionBuilder {
                        addDefaultText()
                        customImage(initialOnlyDemo)
                    }

                    controller = tickBox()

                    updateDemo { v, demo -> (demo as? FirstPersonDemo)?.keepHandFov = !v }
                }
            }

            val scrolling by groups.registering {
                val innerScrollOpts = mutableListOf<Option<*>>()

                val scrollZoomOpt = options.registerDemo(ZoomifySettings::scrollZoom, scrollOnlyDemo) {
                    controller = tickBox()

                    listener { _, v -> innerScrollOpts.forEach { it.setAvailable(v) } }
                }

                options.registerDemo(ZoomifySettings::scrollZoomAmount, scrollOnlyDemo) {
                    controller = slider(range = 1..10)
                }.also { innerScrollOpts.add(it) }

                options.registerDemo(ZoomifySettings::scrollZoomSmoothness, scrollOnlyDemo) {
                    controller = slider(range = 0..100, step = 1, formatter = { v: Int ->
                        if (v == 0)
                            Component.translatable("zoomify.gui.formatter.instant")
                        else
                            Component.literal("%d%%".format(v))
                    })
                }.also { innerScrollOpts.add(it) }

                options.registerDemo(ZoomifySettings::linearLikeSteps, scrollOnlyDemo) {
                    controller = tickBox()
                }.also { innerScrollOpts.add(it) }

                options.registerDemo(ZoomifySettings::retainZoomSteps, scrollOnlyDemo) {
                    controller = tickBox()
                }.also { innerScrollOpts.add(it) }

                innerScrollOpts.forEach { it.setAvailable(scrollZoomOpt.pendingValue()) }
            }

            val spyglass by groups.registering {
                options.register(ZoomifySettings.spyglassBehaviour) {
                    defaultDescription()
                    controller = enumSwitch()
                }

                options.register(ZoomifySettings.spyglassOverlayVisibility) {
                    defaultDescription()
                    controller = enumSwitch()
                }

                options.register(ZoomifySettings.spyglassSoundBehaviour) {
                    defaultDescription()
                    controller = enumSwitch()
                }
            }
        }

        val controls by categories.registering {
            rootOptions.register(ZoomifySettings.zoomKeyBehaviour) {
                defaultDescription()
                controller = enumSwitch()
            }

            rootOptions.register(ZoomifySettings._keybindScrolling) {
                defaultDescription()
                controller = tickBox()
                flag(OptionFlag.GAME_RESTART)
            }

            rootOptions.register(ZoomifySettings.relativeSensitivity) {
                defaultDescription()
                controller = slider(range = 0..150, step = 10, formatter = { v: Int ->
                    if (v == 0)
                        CommonComponents.OPTION_OFF
                    else
                        Component.literal("%d%%".format(v))
                })
            }

            rootOptions.register(ZoomifySettings.relativeViewBobbing) {
                defaultDescription()
                controller = tickBox()
            }

            rootOptions.register(ZoomifySettings.cinematicCamera) {
                defaultDescription()
                controller = slider(range = 0..250, step = 10, formatter = { v: Int ->
                    if (v == 0)
                        CommonComponents.OPTION_OFF
                    else
                        Component.literal("%d%%".format(v))
                })
            }
        }

        val secondary by categories.registering {
            val infoLabel by rootOptions.registeringLabel

            rootOptions.registerDemo(ZoomifySettings::secondaryZoomAmount, secondaryZoomDemo) {
                controller = slider(range = 2..10, step = 1, formatter = formatPercent())
            }

            rootOptions.registerDemo(ZoomifySettings::secondaryZoomInTime, secondaryZoomDemo) {
                controller = slider(range = 6.0..30.0, step = 2.0, formatter = formatSeconds())
            }

            rootOptions.registerDemo(ZoomifySettings::secondaryZoomOutTime, secondaryZoomDemo) {
                controller = slider(range = 0.0..5.0, step = 0.25, formatter = {
                    if (it == 0.0)
                        Component.translatable("zoomify.gui.formatter.instant")
                    else
                        Component.translatable("zoomify.gui.formatter.seconds", "%.2f".format(it))
                })
            }

            rootOptions.register(ZoomifySettings.secondaryHideHUDOnZoom) {
                descriptionBuilder {
                    addDefaultText()
                    customImage(secondaryZoomDemo)
                }

                controller = tickBox()

                updateDemo { v, demo -> (demo as? ThirdPersonDemo)?.renderHud = !v }
            }
        }

        val misc by categories.registering {
            val unbindConflicting by rootOptions.registeringButton {
                defaultDescription()
                action { _, _ ->
                    Zoomify.unbindConflicting()
                }
            }

            val checkMigrations by rootOptions.registeringButton {
                defaultDescription()
                action { _, _ ->
                    if (!Migrator.checkMigrations()) {
                        toast(
                            Component.translatable("zoomify.gui.title"),
                            Component.translatable("zoomify.migrate.no_migrations")
                        )
                    }
                }
            }

            val presets by groups.registering {
                val applyWarning by options.registeringLabel

                val buttonKey = "$groupKey.presetBtn"
                for (preset in Presets.entries) {
                    // create a new settings and apply the preset to it
                    val settings = ZoomifySettings()
                    preset.apply(settings)

                    // create a demo with the new settings
                    val zoomHelper = RegularZoomHelper(settings)
                    val demo = FirstPersonDemo(zoomHelper, ControlEmulation.InitialOnly)

                    options.registerButton(preset.name.lowercase()) {
                        name(preset.displayName)

                        descriptionBuilder {
                            text(Component.translatable("$buttonKey.description", preset.displayName))
                            customImage(demo)
                        }

                        text(Component.translatable("$buttonKey.button"))

                        action { screen, _ ->
                            val minecraft = Minecraft.getInstance()
                            preset.apply(ZoomifySettings)

                            toast(
                                Component.translatable("zoomify.gui.preset.toast.title"),
                                Component.translatable("zoomify.gui.preset.toast.description", preset.displayName)
                            )

                            OptionUtils.forEachOptions(screen.config, Option<*>::forgetPendingValue)
                            ZoomifySettings.saveToFile()
                            screen.init(minecraft, screen.width, screen.height)
                        }
                    }
                }
            }
        }
    }.generateScreen(parent)
}

private fun <T : Number> formatSeconds() = ValueFormatter<T> {
    Component.translatable("zoomify.gui.formatter.seconds", "%.1f".format(it))
}

private fun <T : Number> formatPercent() = ValueFormatter<T> {
    Component.literal("%dx".format(it))
}

private fun OptionDsl<*>.defaultDescription() {
    descriptionBuilder {
        addDefaultText()
    }
}

private fun ButtonOptionDsl.defaultDescription() {
    descriptionBuilder {
        addDefaultText()
    }
}
