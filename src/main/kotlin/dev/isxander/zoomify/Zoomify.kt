package dev.isxander.zoomify

import dev.isxander.zoomify.config.SpyglassBehaviour
import dev.isxander.zoomify.config.ZoomKeyBehaviour
import dev.isxander.zoomify.config.ZoomifySettings
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.item.Items
import org.slf4j.LoggerFactory

object Zoomify : ClientModInitializer {
    val LOGGER = LoggerFactory.getLogger("Zoomify")!!

    private val guiKey = KeyBinding("zoomify.key.gui", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_F8, "zoomify.key.category")
    private val zoomKey = KeyBinding("zoomify.key.zoom", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_C, "zoomify.key.category")

    var zooming = false

    private val zoomHelper = ZoomHelper()

    var previousZoomDivisor = 1.0
        private set

    const val maxScrollTiers = 30
    private var scrollSteps = 0

    override fun onInitializeClient() {
        // imports on <init>
        ZoomifySettings

        KeyBindingHelper.registerKeyBinding(zoomKey)
        KeyBindingHelper.registerKeyBinding(guiKey)

        ClientTickEvents.END_CLIENT_TICK.register(this::tick)
    }

    private fun tick(client: MinecraftClient) {
        val cameraEntity = client.cameraEntity

        when (ZoomifySettings.zoomKeyBehaviour) {
            ZoomKeyBehaviour.HOLD -> zooming = zoomKey.isPressed
            ZoomKeyBehaviour.TOGGLE -> {
                while (zoomKey.wasPressed()) {
                    zooming = !zooming
                }
            }
        }

        if (cameraEntity is AbstractClientPlayerEntity) {
            if (ZoomifySettings.spyglassBehaviour == SpyglassBehaviour.ONLY_ZOOM_WHILE_HOLDING && !cameraEntity.isHolding(Items.SPYGLASS))
                zooming = false
            if (ZoomifySettings.spyglassBehaviour == SpyglassBehaviour.ONLY_ZOOM_WHILE_CARRYING && !cameraEntity.inventory.containsAny { it.isOf(Items.SPYGLASS) })
                zooming = false

            val requiresSpyglass = ZoomifySettings.spyglassBehaviour != SpyglassBehaviour.COMBINE
            zooming = zooming || (requiresSpyglass && client.options.perspective.isFirstPerson && cameraEntity.isUsingSpyglass)
        }

        if (!zooming) {
            scrollSteps = 0
            zoomHelper.reset()
        }

        zoomHelper.tick(zooming, scrollSteps)

        while (guiKey.wasPressed()) {
            client.setScreen(ZoomifySettings.gui(client.currentScreen))
        }
    }

    @JvmStatic
    fun getZoomDivisor(tickDelta: Float = 1f): Double {
        return zoomHelper.getZoomDivisor(tickDelta).also { previousZoomDivisor = it }
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
