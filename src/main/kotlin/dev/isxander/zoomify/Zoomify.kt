package dev.isxander.zoomify

import dev.isxander.zoomify.config.*
import dev.isxander.zoomify.config.migrator.Migrator
import dev.isxander.zoomify.zoom.*
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
import net.minecraft.util.math.MathHelper
import org.slf4j.LoggerFactory

object Zoomify : ClientModInitializer {
    val LOGGER = LoggerFactory.getLogger("Zoomify")!!

    private val zoomKey = KeyBinding("zoomify.key.zoom", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_C, "zoomify.key.category")
    private val secondaryZoomKey = KeyBinding("zoomify.key.zoom.secondary", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_F6, "zoomify.key.category")
    private val scrollZoomIn = KeyBinding("zoomify.key.zoom.in", -1, "zoomify.key.category")
    private val scrollZoomOut = KeyBinding("zoomify.key.zoom.out", -1, "zoomify.key.category")

    var zooming = false
        private set
    private val zoomHelper = ZoomHelper(
        TransitionInterpolator(
            ZoomifySettings::zoomInTransition,
            ZoomifySettings::zoomOutTransition,
            ZoomifySettings::zoomInTime,
            ZoomifySettings::zoomOutTime
        ),
        SmoothInterpolator {
            MathHelper.lerp(
                ZoomifySettings.scrollZoomSmoothness / 100.0,
                1.0,
                0.1
            )
        },
        initialZoom = ZoomifySettings::initialZoom,
        scrollZoomAmount = ZoomifySettings::scrollZoomAmount,
        maxScrollTiers = Zoomify::maxScrollTiers,
        linearLikeSteps = ZoomifySettings::linearLikeSteps,
    )

    var secondaryZooming = false
        private set
    private val secondaryZoomHelper = ZoomHelper(
        TimedInterpolator(ZoomifySettings::secondaryZoomInTime, ZoomifySettings::secondaryZoomOutTime),
        InstantInterpolator,
        initialZoom = ZoomifySettings::secondaryZoomAmount,
        scrollZoomAmount = { 0 },
        maxScrollTiers = { 0 },
        linearLikeSteps = { false },
    )

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
        KeyBindingHelper.registerKeyBinding(secondaryZoomKey)
        if (ZoomifySettings.keybindScrolling) {
            KeyBindingHelper.registerKeyBinding(scrollZoomIn)
            KeyBindingHelper.registerKeyBinding(scrollZoomOut)
        }

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
        val prevZooming = zooming

        when (ZoomifySettings.zoomKeyBehaviour) {
            ZoomKeyBehaviour.HOLD -> zooming = zoomKey.isPressed
            ZoomKeyBehaviour.TOGGLE -> {
                while (zoomKey.wasPressed()) {
                    zooming = !zooming
                }
            }
        }

        while (secondaryZoomKey.wasPressed()) {
            secondaryZooming = !secondaryZooming
        }

        if (ZoomifySettings.keybindScrolling) {
            while (scrollZoomIn.wasPressed()) {
                scrollSteps++
            }
            while (scrollZoomOut.wasPressed()) {
                scrollSteps--
            }

            scrollSteps = scrollSteps.coerceIn(0..maxScrollTiers)
        }

        handleSpyglass(client, prevZooming)

        zoomHelper.tick(zooming, scrollSteps)
        secondaryZoomHelper.tick(secondaryZooming, scrollSteps)

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

        return zoomHelper.getZoomDivisor(tickDelta).also { previousZoomDivisor = it } * secondaryZoomHelper.getZoomDivisor(tickDelta)
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

    private fun handleSpyglass(client: MinecraftClient, prevZooming: Boolean) {
        val cameraEntity = client.cameraEntity

        if (cameraEntity is AbstractClientPlayerEntity) {
            when (ZoomifySettings.spyglassBehaviour) {
                SpyglassBehaviour.ONLY_ZOOM_WHILE_HOLDING -> {
                    if (!cameraEntity.isHolding(Items.SPYGLASS))
                        zooming = false
                }
                SpyglassBehaviour.ONLY_ZOOM_WHILE_CARRYING ->
                    if (!cameraEntity.inventory.containsAny { it.isOf(Items.SPYGLASS) })
                        zooming = false
                SpyglassBehaviour.OVERRIDE ->
                    if (cameraEntity.isUsingSpyglass)
                        zooming = zooming && client.options.perspective.isFirstPerson
                else -> {}
            }

            val requiresSpyglass = ZoomifySettings.spyglassBehaviour != SpyglassBehaviour.COMBINE
            if (requiresSpyglass && cameraEntity.isUsingSpyglass) {
                zooming = true
            }

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
            LOGGER.info("Zoomify detected first launch!")
            detectConflictingToast()

            Migrator.checkMigrations()
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
                SystemToast.Type.UNSECURE_SERVER_WARNING,
                Text.translatable("zoomify.toast.conflictingKeybind.title"),
                Text.translatable("zoomify.toast.conflictingKeybind.description",
                    Text.translatable("zoomify.gui.category.misc")
                )
            )
            client.toastManager.add(toast)
        }
    }
}
