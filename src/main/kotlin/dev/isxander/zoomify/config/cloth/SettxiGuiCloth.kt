package dev.isxander.zoomify.config.cloth

import dev.isxander.settxi.impl.*
import dev.isxander.settxi.SettxiConfig
import dev.isxander.settxi.gui.GuiSettingRegistry
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry
import me.shedaniel.clothconfig2.impl.builders.EnumSelectorBuilder
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import java.io.File
import java.nio.file.Paths
import java.util.Optional
import java.util.function.Supplier
import kotlin.io.path.notExists

/**
 * Constructs a Cloth Config gui
 *
 * @param title GUI Title, displayed at the top of the [Screen]
 * @param parent [Screen] to open once the Cloth Config screen is closed
 * @param builder Access the Cloth Config [ConfigBuilder] for custom logic.
 */
fun SettxiConfig.clothGui(title: Text, parent: Screen? = null, builder: ConfigBuilder.() -> Unit = {}): Screen =
    ConfigBuilder.create().apply {
        this.parentScreen = parent
        this.title = title

        for (setting in settings) {
            if (setting.hidden) continue

            val category = getOrCreateCategory(TranslatableText(setting.category))
            category.addEntry(SettxiClothConfigGui.buildEntryForSetting(this, setting))
        }

        setSavingRunnable { export() }

        builder()
    }.build()

object SettxiClothConfigGui : GuiSettingRegistry<ConfigBuilder, TooltipListEntry<out Any>>() {
    init {
        registerType<BooleanSetting> { setting ->
            entryBuilder().startBooleanToggle(TranslatableText(setting.name), setting.get(false)).apply {
                defaultValue = Supplier { setting.default }
                setting.description?.let{ setTooltip(TranslatableText(it)) }
                setSaveConsumer { setting.set(it, false) }
                requireRestart(setting.clothRequireRestart)
                setting.clothYesNoText?.let { setYesNoTextSupplier(it) }
            }.build()
        }
        registerType<DoubleSetting> { setting ->
            entryBuilder().startDoubleField(TranslatableText(setting.name), setting.get(false)).apply {
                defaultValue = Supplier { setting.default }
                setting.description?.let{ setTooltip(TranslatableText(it)) }
                setSaveConsumer { setting.set(it, false) }
                requireRestart(setting.clothRequireRestart)
                if (setting.range != null) {
                    setMin(setting.range!!.start)
                    setMax(setting.range!!.endInclusive)
                }
            }.build()
        }
        registerType<FloatSetting> { setting ->
            entryBuilder().startFloatField(TranslatableText(setting.name), setting.get(false)).apply {
                defaultValue = Supplier { setting.default }
                setting.description?.let{ setTooltip(TranslatableText(it)) }
                setSaveConsumer { setting.set(it, false) }
                requireRestart(setting.clothRequireRestart)
                if (setting.range != null) {
                    setMin(setting.range!!.start)
                    setMax(setting.range!!.endInclusive)
                }
            }.build()
        }
        registerType<LongSetting> { setting ->
            if (setting.range != null) {
                entryBuilder().startLongSlider(
                    TranslatableText(setting.name),
                    setting.get(false),
                    setting.range!!.first,
                    setting.range!!.last
                ).apply {
                    defaultValue = Supplier { setting.default }
                    setting.description?.let{ setTooltip(TranslatableText(it)) }
                    setSaveConsumer { setting.set(it, false) }
                    requireRestart(setting.clothRequireRestart)
                }.build().apply {
                    setting.clothTextGetter?.let { setTextGetter(it) }
                }
            } else {
                entryBuilder().startLongField(
                    TranslatableText(setting.name),
                    setting.get(false),
                ).apply {
                    defaultValue = Supplier { setting.default }
                    setting.description?.let{ setTooltip(TranslatableText(it)) }
                    setSaveConsumer { setting.set(it, false) }
                    requireRestart(setting.clothRequireRestart)
                }.build()
            }
        }
        registerType<IntSetting> { setting ->
            if (setting.range != null) {
                entryBuilder().startIntSlider(
                    TranslatableText(setting.name),
                    setting.get(false),
                    setting.range!!.first,
                    setting.range!!.last
                ).apply {
                    defaultValue = Supplier { setting.default }
                    setting.description?.let{ setTooltip(TranslatableText(it)) }
                    setSaveConsumer { setting.set(it, false) }
                    requireRestart(setting.clothRequireRestart)
                }.build().apply {
                    setting.clothTextGetter?.let { setTextGetter(it) }
                }
            } else {
                entryBuilder().startIntField(
                    TranslatableText(setting.name),
                    setting.get(false),
                ).apply {
                    defaultValue = Supplier { setting.default }
                    setting.description?.let{ setTooltip(TranslatableText(it)) }
                    setSaveConsumer { setting.set(it, false) }
                    requireRestart(setting.clothRequireRestart)
                }.build()
            }
        }
        registerType<StringSetting> { setting ->
            entryBuilder().startStrField(TranslatableText(setting.name), setting.get(false)).apply {
                defaultValue = Supplier { setting.default }
                setting.description?.let{ setTooltip(TranslatableText(it)) }
                setSaveConsumer { setting.set(it, false) }
                requireRestart(setting.clothRequireRestart)
            }.build()
        }
        registerType<EnumSetting<*>> { setting ->
            setting.toEnumSelector(entryBuilder()).apply {
                requireRestart(setting.clothRequireRestart)
            }.build()
        }
        registerType<FileSetting> { setting ->
            entryBuilder().startStrField(TranslatableText(setting.name), setting.get(false).absolutePath).apply {
                defaultValue = Supplier { setting.default.absolutePath }
                setting.description?.let{ setTooltip(TranslatableText(it)) }
                setSaveConsumer { setting.set(File(it).absoluteFile, false) }
                requireRestart(setting.clothRequireRestart)
                setErrorSupplier {
                    if (!File(it).exists())
                        Optional.of(TranslatableText("settxi.cloth.file_not_exists"))
                    else
                        Optional.empty()
                }
            }.build()
        }
        registerType<PathSetting> { setting ->
            entryBuilder().startStrField(TranslatableText(setting.name), setting.get(false).toAbsolutePath().toString()).apply {
                defaultValue = Supplier { setting.default.toAbsolutePath().toString() }
                setting.description?.let{ setTooltip(TranslatableText(it)) }
                setSaveConsumer { setting.set(Paths.get(it), false) }
                requireRestart(setting.clothRequireRestart)
                setErrorSupplier {
                    if (Paths.get(it).notExists())
                        Optional.of(TranslatableText("settxi.cloth.file_not_exists"))
                    else
                        Optional.empty()
                }
            }.build()
        }
    }

    @Suppress("unchecked_cast")
    private fun <T : Enum<T>> EnumSetting<T>.toEnumSelector(entryBuilder: ConfigEntryBuilder): EnumSelectorBuilder<T> =
        entryBuilder.startEnumSelector(TranslatableText(name), enumClass, get(false)).apply {
            defaultValue = Supplier { default }
            description?.let{ setTooltip(TranslatableText(it)) }
            setSaveConsumer { set(it, false) }
            setEnumNameProvider { TranslatableText(nameProvider(it as T)) }
        }
}
