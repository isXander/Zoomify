package dev.isxander.zoomify

import dev.isxander.zoomify.config.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.SoundEvents
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

    private var shouldPlaySound = false

    override fun onInitializeClient() {
        // imports on <init>
        ZoomifySettings

        KeyBindingHelper.registerKeyBinding(zoomKey)
        KeyBindingHelper.registerKeyBinding(guiKey)

        ClientTickEvents.END_CLIENT_TICK.register(this::tick)
    }

    private fun tick(client: MinecraftClient) {
        val cameraEntity = client.cameraEntity
        val prevZooming = zooming

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

            if (shouldPlaySound) {
                if (!zooming && prevZooming) {
                    cameraEntity.playSound(SoundEvents.ITEM_SPYGLASS_STOP_USING, 1f, 1f)
                }
            }

            shouldPlaySound = when (ZoomifySettings.spyglassSoundBehaviour) {
                SoundBehaviour.NEVER -> false
                SoundBehaviour.ALWAYS -> true
                SoundBehaviour.ONLY_SPYGLASS -> cameraEntity.isUsingSpyglass || (requiresSpyglass && zooming && cameraEntity.isHolding(Items.SPYGLASS))
                SoundBehaviour.WITH_OVERLAY -> shouldRenderOverlay(
                    cameraEntity,
                    client.options.perspective.isFirstPerson && cameraEntity.isUsingSpyglass
                ) && requiresSpyglass
            }

            if (shouldPlaySound) {
                if (zooming && !prevZooming) {
                    cameraEntity.playSound(SoundEvents.ITEM_SPYGLASS_USE, 1f, 1f)
                }
            }
        }

        while (guiKey.wasPressed()) {
            client.setScreen(ZoomifySettings.gui(client.currentScreen))
        }


    }

    @JvmStatic
    fun getZoomDivisor(): Double {
        if (!zooming) {
            scrollSteps = 0
            zoomHelper.reset()
        }

        // tick every frame so fps isn't
        zoomHelper.tick(zooming, scrollSteps, MinecraftClient.getInstance().lastFrameDuration * 50 / 1000.0)

        return zoomHelper.getZoomDivisor().also { previousZoomDivisor = it }
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

    @JvmStatic
    fun shouldRenderOverlay(player: AbstractClientPlayerEntity, isUsingSpyglass: Boolean) =
        when (ZoomifySettings.spyglassOverlayVisibility) {
            OverlayVisibility.NEVER -> false
            OverlayVisibility.ALWAYS -> zooming
            OverlayVisibility.HOLDING -> isUsingSpyglass
                    || zooming
                    && player.isHolding(Items.SPYGLASS)
                    && ZoomifySettings.spyglassBehaviour != SpyglassBehaviour.COMBINE
            OverlayVisibility.CARRYING -> zooming
                    && player.inventory.containsAny { stack: ItemStack -> stack.isOf(Items.SPYGLASS) }
        }
}
