package dev.isxander.zoomify

import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.utils.TransitionType
import dev.isxander.zoomify.utils.getPercent
import dev.isxander.zoomify.utils.lerp
import gg.essential.vigilance.Vigilance
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.math.MathHelper

object Zoomify : ClientModInitializer {
    val zoomKey = KeyBinding("zoomify.zoom", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_C, "zoomify.category")
    private var lastZoomMultiplier = 0.0
    private var scrollSteps = 0

    override fun onInitializeClient() {
        Vigilance.initialize()
        ZoomifySettings.preload()

        KeyBindingHelper.registerKeyBinding(zoomKey)
    }

    @JvmStatic
    fun getZoomDivisor(tickDelta: Float = 1f): Double {
        if (!zoomKey.isPressed) scrollSteps = 0

        val scrollAmt = TransitionType.EASE_OUT_QUAD.apply(getPercent(scrollSteps.toDouble(), 0.0, 6.0)) * 1.5
        val targetZoom = if (zoomKey.isPressed) 1.0 + scrollAmt else 0.0
        lastZoomMultiplier = lerp(lastZoomMultiplier, targetZoom, tickDelta * (ZoomifySettings.zoomSpeed.toDouble() / 50.0))

        return lerp(1.0, ZoomifySettings.initialZoom.toDouble(), ZoomifySettings.zoomTransition.apply(lastZoomMultiplier))
    }

    @JvmStatic
    fun mouseZoom(mouseDelta: Double) {
        if (mouseDelta > 0) {
            scrollSteps++
        } else if (mouseDelta < 0) {
            scrollSteps--
        }
        scrollSteps = scrollSteps.coerceIn(0..6)
    }
}
