package dev.isxander.zoomify.config.settxi

import dev.isxander.settxi.Setting
import dev.isxander.settxi.ConfigProcessor
import dev.isxander.settxi.gui.spruce.SettxiSpruceUIGui
import dev.isxander.settxi.serialization.ObjectType
import dev.isxander.settxi.serialization.SerializedType
import dev.lambdaurora.spruceui.option.SpruceSimpleActionOption
import net.minecraft.text.Text

class ButtonSetting internal constructor(
    action: () -> Unit,
    lambda: ButtonSetting.() -> Unit = {},
) : Setting<() -> Unit>(action) {
    override lateinit var name: String
    override lateinit var category: String
    override var description: String? = null
    override val shouldSave: Boolean = true

    override var serializedValue: SerializedType
        get() = ObjectType()
        set(new) {}

    override var defaultSerializedValue: (root: ObjectType, category: ObjectType?) -> SerializedType = { _, _ -> ObjectType() }

    init {
        this.apply(lambda)
    }

    companion object {
        fun registerSpruceUI() {
            SettxiSpruceUIGui.registerType<ButtonSetting> { setting ->
                SpruceSimpleActionOption.of(
                    setting.name,
                    { setting.get(false).invoke() },
                    setting.description?.let { Text.translatable(it) }
                )
            }
        }
    }
}

/**
 * Constructs and registers a [ButtonSetting]
 */
@JvmName("buttonSetting")
fun ConfigProcessor.button(action: () -> Unit, lambda: ButtonSetting.() -> Unit): ButtonSetting {
    return ButtonSetting(action, lambda).also { settings.add(it) }
}
