package dev.isxander.zoomify

import com.mojang.blaze3d.platform.InputConstants
import dev.isxander.zoomify.config.*
import dev.isxander.zoomify.config.migrator.Migrator
import dev.isxander.zoomify.integrations.constrainModVersionIfLoaded
import dev.isxander.zoomify.utils.toast
import dev.isxander.zoomify.zoom.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.slf4j.LoggerFactory

object Zoomify : ClientModInitializer {
    val LOGGER = LoggerFactory.getLogger("Zoomify")!!

    private val zoomKey = KeyMapping("zoomify.key.zoom", InputConstants.Type.KEYSYM, InputConstants.KEY_C, "zoomify.key.category")
    private val secondaryZoomKey = KeyMapping("zoomify.key.zoom.secondary", InputConstants.Type.KEYSYM, InputConstants.KEY_F6, "zoomify.key.category")
    private val scrollZoomIn = KeyMapping("zoomify.key.zoom.in", -1, "zoomify.key.category")
    private val scrollZoomOut = KeyMapping("zoomify.key.zoom.out", -1, "zoomify.key.category")

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
            Mth.lerp(
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
        constrainModVersionIfLoaded("controlify", "2.x.x")

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

    private fun tick(minecraft: Minecraft) {
        val prevZooming = zooming

        when (ZoomifySettings.zoomKeyBehaviour) {
            ZoomKeyBehaviour.HOLD -> zooming = zoomKey.isDown
            ZoomKeyBehaviour.TOGGLE -> {
                while (zoomKey.consumeClick()) {
                    zooming = !zooming
                }
            }
        }

        while (secondaryZoomKey.consumeClick()) {
            secondaryZooming = !secondaryZooming
        }

        if (ZoomifySettings.keybindScrolling) {
            while (scrollZoomIn.consumeClick()) {
                scrollSteps++
            }
            while (scrollZoomOut.consumeClick()) {
                scrollSteps--
            }

            scrollSteps = scrollSteps.coerceIn(0..maxScrollTiers)
        }

        handleSpyglass(minecraft, prevZooming)

        zoomHelper.tick(zooming, scrollSteps)
        secondaryZoomHelper.tick(secondaryZooming, scrollSteps)

        if (displayGui) {
            displayGui = false
            minecraft.setScreen(createSettingsGui(minecraft.screen))
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

    private fun handleSpyglass(minecraft: Minecraft, prevZooming: Boolean) {
        val cameraEntity = minecraft.cameraEntity

        if (cameraEntity is AbstractClientPlayer) {
            when (ZoomifySettings.spyglassBehaviour) {
                SpyglassBehaviour.ONLY_ZOOM_WHILE_HOLDING -> {
                    if (!cameraEntity.isHolding(Items.SPYGLASS))
                        zooming = false
                }
                SpyglassBehaviour.ONLY_ZOOM_WHILE_CARRYING ->
                    if (!cameraEntity.inventory.hasAnyMatching { it.`is`(Items.SPYGLASS) })
                        zooming = false
                SpyglassBehaviour.OVERRIDE ->
                    if (cameraEntity.isScoping)
                        zooming = zooming && minecraft.options.cameraType.isFirstPerson
                else -> {}
            }

            val requiresSpyglass = ZoomifySettings.spyglassBehaviour != SpyglassBehaviour.COMBINE
            if (requiresSpyglass && cameraEntity.isScoping) {
                zooming = true
            }

            if (shouldPlaySound) {
                if (!zooming && prevZooming) {
                    cameraEntity.playSound(SoundEvents.SPYGLASS_STOP_USING, 1f, 1f)
                }
            }

            shouldPlaySound = when (ZoomifySettings.spyglassSoundBehaviour) {
                SoundBehaviour.NEVER -> false
                SoundBehaviour.ALWAYS -> true
                SoundBehaviour.ONLY_SPYGLASS -> cameraEntity.isScoping || (requiresSpyglass && zooming && cameraEntity.isHolding(Items.SPYGLASS))
                SoundBehaviour.WITH_OVERLAY -> shouldRenderOverlay(
                    cameraEntity,
                    minecraft.options.cameraType.isFirstPerson && cameraEntity.isScoping
                ) && requiresSpyglass
            }

            if (shouldPlaySound) {
                if (zooming && !prevZooming) {
                    cameraEntity.playSound(SoundEvents.SPYGLASS_USE, 1f, 1f)
                }
            }
        }
    }

    @JvmStatic
    fun shouldRenderOverlay(player: AbstractClientPlayer, isUsingSpyglass: Boolean) =
        when (ZoomifySettings.spyglassOverlayVisibility) {
            OverlayVisibility.NEVER -> false
            OverlayVisibility.ALWAYS -> zooming
            OverlayVisibility.HOLDING -> isUsingSpyglass
                    || (zooming && player.isHolding(Items.SPYGLASS))
                    && ZoomifySettings.spyglassBehaviour != SpyglassBehaviour.COMBINE
            OverlayVisibility.CARRYING -> zooming
                    && player.inventory.hasAnyMatching { stack: ItemStack -> stack.`is`(Items.SPYGLASS) }
        }

    fun onGameFinishedLoading() {
        if (ZoomifySettings.firstLaunch) {
            LOGGER.info("Zoomify detected first launch!")
            detectConflictingToast()

            Migrator.checkMigrations()
        }
    }

    fun unbindConflicting(): Boolean {
        val minecraft = Minecraft.getInstance()
        if (!zoomKey.isUnbound) {
            for (key in minecraft.options.keyMappings) {
                if (key != zoomKey && key.equals(zoomKey)) {
                    minecraft.options.setKey(key, InputConstants.UNKNOWN)

                    toast(
                        Component.translatable("zoomify.toast.unbindConflicting.name"),
                        Component.translatable("zoomify.toast.unbindConflicting.description",
                            Component.translatable(key.name)
                        ),
                        longer = false
                    )

                    return true
                }
            }
        }

        return false
    }

    private fun detectConflictingToast() {
        val minecraft = Minecraft.getInstance()

        if (zoomKey.isUnbound)
            return

        if (minecraft.options.keyMappings.any { it != zoomKey && it.equals(zoomKey) }) {
            toast(
                Component.translatable("zoomify.toast.conflictingKeybind.title"),
                Component.translatable("zoomify.toast.conflictingKeybind.description",
                    Component.translatable("zoomify.gui.category.misc")
                ),
                longer = true
            )
        }
    }
}
