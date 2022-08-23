package dev.isxander.zoomify

import dev.isxander.zoomify.config.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.toast.SystemToast
import net.minecraft.client.util.InputUtil
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import org.slf4j.LoggerFactory

object Zoomify : ClientModInitializer {
    val LOGGER = LoggerFactory.getLogger("Zoomify")!!

    val zoomKey = KeyBinding("zoomify.key.zoom", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_C, "zoomify.key.category")

    var zooming = false
    private val zoomHelper = ZoomHelper()

    var previousZoomDivisor = 1.0
        private set

    const val maxScrollTiers = 30
    private var scrollSteps = 0

    private var shouldPlaySound = false

    private var displayGui = false

    override fun onInitializeClient() {
        // imports on <init>
        ZoomifySettings

        KeyBindingHelper.registerKeyBinding(zoomKey)

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("zoomify").executes {
                    displayGui = true
                    0
                }
            )
        }

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

        zoomHelper.tick(zooming, scrollSteps)

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

        if (displayGui) {
            displayGui = false
            client.setScreen(ZoomifySettings.gui(client.currentScreen))
        }
    }

    @JvmStatic
    fun getZoomDivisor(tickDelta: Float): Double {
        if (!zooming) {
            if (!ZoomifySettings.retainZoomSteps)
                scrollSteps = 0

            zoomHelper.reset()
        }

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

    @JvmStatic
    fun shouldRenderOverlay(player: AbstractClientPlayerEntity, isUsingSpyglass: Boolean) =
        when (ZoomifySettings.spyglassOverlayVisibility) {
            OverlayVisibility.NEVER -> false
            OverlayVisibility.ALWAYS -> zooming
            OverlayVisibility.HOLDING -> isUsingSpyglass
                    || (zooming && player.isHolding(Items.SPYGLASS))
                    && ZoomifySettings.spyglassBehaviour != SpyglassBehaviour.COMBINE
            OverlayVisibility.CARRYING -> zooming
                    && player.inventory.containsAny { stack: ItemStack -> stack.isOf(Items.SPYGLASS) }
        }

    fun onGameFinishedLoading() {
        if (ZoomifySettings.firstLaunch) {
            LOGGER.info("Zoomify detected first launch! Detecting conflicting keybindings!")
            detectConflictingToast()
        }
    }

    fun unbindConflicting(): Boolean {
        val client = MinecraftClient.getInstance()
        if (!zoomKey.isUnbound) {
            for (key in client.options.allKeys) {
                if (key != zoomKey && key.equals(zoomKey)) {
                    client.options.setKeyCode(key, InputUtil.UNKNOWN_KEY)

                    val toast = SystemToast.create(
                        client,
                        SystemToast.Type.TUTORIAL_HINT, // doesn't do anything except toast duration
                        Text.translatable("zoomify.toast.unbindConflicting.name"),
                        Text.translatable("zoomify.toast.unbindConflicting.description",
                            Text.translatable(key.translationKey)
                        )
                    )
                    client.toastManager.add(toast)

                    return true
                }
            }
        }

        return false
    }

    private fun detectConflictingToast() {
        val client = MinecraftClient.getInstance()

        if (zoomKey.isUnbound)
            return

        if (client.options.allKeys.any { it != zoomKey && it.equals(zoomKey) }) {
            val toast = SystemToast.create(
                client,
                SystemToast.Type.CHAT_PREVIEW_WARNING,
                Text.translatable("zoomify.toast.conflictingKeybind.title"),
                Text.translatable("zoomify.toast.conflictingKeybind.description",
                    Text.translatable("zoomify.gui.category.misc"))
            )
            client.toastManager.add(toast)
        }
    }
}
