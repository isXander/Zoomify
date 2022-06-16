package dev.isxander.zoomify

import dev.isxander.zoomify.config.ZoomKeyBehaviour
import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.zoom.SingleZoomHelper
import dev.isxander.zoomify.zoom.TieredZoomHelper
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.slf4j.LoggerFactory

object Zoomify : ClientModInitializer {
    val LOGGER = LoggerFactory.getLogger("Zoomify")

    val guiKey = KeyBinding("zoomify.key.gui", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_F8, "zoomify.key.category")
    val zoomKey = KeyBinding("zoomify.key.zoom", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_C, "zoomify.key.category")

    var zooming = false

    private val normalZoomHelper = SingleZoomHelper()
    private val scrollZoomHelper = TieredZoomHelper()
    private var scrollSteps = 0

    var previousZoomDivisor = 1.0
        private set

    val maxScrollTiers = 20

    override fun onInitializeClient() {
        ZoomifySettings.load()

        KeyBindingHelper.registerKeyBinding(zoomKey)
        KeyBindingHelper.registerKeyBinding(guiKey)

        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            if (zoomKey.wasPressed() && ZoomifySettings.zoomKeyBehaviour == ZoomKeyBehaviour.TOGGLE) {
                zooming = !zooming
            }

            if (guiKey.wasPressed()) {
                mc.setScreen(ZoomifySettings.clothGui(mc.currentScreen))
            }
        }
    }

    @JvmStatic
    fun getZoomDivisor(tickDelta: Float = 1f): Double {
        if (ZoomifySettings.zoomKeyBehaviour == ZoomKeyBehaviour.HOLD)
            zooming = zoomKey.isPressed

        if (!zooming) {
            scrollSteps = 0
            scrollZoomHelper.resetTiers()
        }

        previousZoomDivisor = (normalZoomHelper.getZoomDivisor(zooming, tickDelta) +
                scrollZoomHelper.getZoomDivisor(scrollSteps, tickDelta)).coerceAtLeast(1.0)

        return previousZoomDivisor
    }

    @JvmStatic
    fun mouseZoom(mouseDelta: Double) {
        if (mouseDelta > 0) {
            scrollSteps++
        } else if (mouseDelta < 0) {
            scrollSteps--
        }
        scrollSteps = scrollSteps.coerceIn(0..maxScrollTiers)
    }
}
