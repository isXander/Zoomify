package dev.isxander.zoomify

import com.llamalad7.mixinextras.MixinExtrasBootstrap
import dev.isxander.zoomify.config.ZoomKeyBehaviour
import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.utils.mc
import dev.isxander.zoomify.zoom.SingleZoomHelper
import dev.isxander.zoomify.zoom.TieredZoomHelper
import gg.essential.vigilance.Vigilance
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

object Zoomify : ClientModInitializer {
    val zoomKey = KeyBinding("zoomify.key.zoom", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_C, "zoomify.key.category")
    var zooming = false

    private val normalZoomHelper = SingleZoomHelper({ ZoomifySettings.initialZoom.toDouble() }, { ZoomifySettings.zoomSpeed.toDouble() }, { ZoomifySettings.zoomTransition })
    private val scrollZoomHelper = TieredZoomHelper({ ZoomifySettings.scrollZoomSpeed.toDouble() }, { ZoomifySettings.scrollZoomTransition }, { 6 }, { ZoomifySettings.maxScrollZoom * 5.0 })
    private var scrollSteps = 0

    override fun onInitializeClient() {
        MixinExtrasBootstrap.init()

        Vigilance.initialize()
        ZoomifySettings.preload()

        KeyBindingHelper.registerKeyBinding(zoomKey)

        ClientTickEvents.END_CLIENT_TICK.register {
            if (zoomKey.wasPressed() && ZoomifySettings.zoomKeyBehaviour == ZoomKeyBehaviour.TOGGLE) {
                zooming = !zooming
            }
        }
    }

    @JvmStatic
    fun getZoomDivisor(tickDelta: Float = 1f): Double {
        if (ZoomifySettings.zoomKeyBehaviour == ZoomKeyBehaviour.HOLD)
            zooming = zoomKey.isPressed

        if (!zooming) scrollSteps = 0

        return normalZoomHelper.getZoomDivisor(SingleZoomHelper.SingleZoomParams(zooming, tickDelta)) +
                scrollZoomHelper.getZoomDivisor(TieredZoomHelper.TieredZoomParams(scrollSteps, tickDelta))
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
