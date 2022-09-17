package dev.isxander.zoomify.config.cloth

import dev.isxander.settxi.Setting
import dev.isxander.settxi.impl.BooleanSetting
import dev.isxander.settxi.impl.IntSetting
import dev.isxander.settxi.impl.LongSetting
import net.minecraft.text.Text

/**
 * When true, Cloth Config suggests the user restarts their game once changed.
 */
var Setting<*>.clothRequireRestart: Boolean
    get() = customProperties["cloth_requireRestart"] as Boolean? == true
    set(value) { customProperties["cloth_requireRestart"] = value }

/**
 * Supplies [Text] for the toggle button depending on if it is true or false.
 */
var BooleanSetting.clothYesNoText: ((Boolean) -> Text)?
    get() = customProperties["cloth_yesNoText"] as ((Boolean) -> Text)?
    set(value) {
        if (value != null)
            customProperties["cloth_yesNoText"] = value
        else
            customProperties.remove("cloth_yesNoText")
    }

/**
 * Supplies [Text] for the slider to tell the user what the value is.
 */
var IntSetting.clothTextGetter: ((Int) -> Text)?
    get() = customProperties["cloth_textGetter"] as ((Int) -> Text)?
    set(value) {
        if (value != null) {
            if (range == null) throw IllegalStateException("`clothTextGetter` only works when `range` is defined")
            customProperties["cloth_textGetter"] = value
        } else {
            customProperties.remove("cloth_textGetter")
        }
    }

/**
 * Supplies [Text] for the slider to tell the user what the value is.
 */
var LongSetting.clothTextGetter: ((Long) -> Text)?
    get() = customProperties["cloth_textGetter"] as ((Long) -> Text)?
    set(value) {
        if (value != null) {
            if (range == null) throw IllegalStateException("`clothTextGetter` only works when `range` is defined")
            customProperties["cloth_textGetter"] = value
        } else {
            customProperties.remove("cloth_textGetter")
        }
    }
