package dev.isxander.zoomify.integrations

import dev.isxander.controlify.api.ControlifyApi
import dev.isxander.controlify.api.bind.ControlifyBindApi
import dev.isxander.controlify.api.bind.InputBinding
import dev.isxander.controlify.api.bind.InputBindingSupplier
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint
import dev.isxander.controlify.api.event.ControlifyEvents
import dev.isxander.controlify.bindings.BindContext
import dev.isxander.controlify.bindings.RadialIcons
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.utils.KeySource
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraft.world.item.Items

object ControlifyIntegration : ControlifyEntrypoint {
    private val category = Component.translatable("zoomify.key.category")

    val ZOOM_HOLD = ControlifyBindApi.get().registerBinding { it.apply {
        id("zoomify", "zoom_hold")
        allowedContexts(BindContext.IN_GAME)
        addKeyCorrelation(Zoomify.zoomHoldKeyMapping)
        category(category)
        name(Component.translatable(Zoomify.zoomHoldKeyMapping.name))
    } }
    val ZOOM_TOGGLE = ControlifyBindApi.get().registerBinding { it.apply {
        id("zoomify", "zoom_toggle")
        allowedContexts(BindContext.IN_GAME)
        addKeyCorrelation(Zoomify.zoomToggleKeyMapping)
        radialCandidate(RadialIcons.getItem(Items.SPYGLASS))
        category(category)
        name(Component.translatable(Zoomify.zoomToggleKeyMapping.name))
    } }
    val SECONDARY_ZOOM = ControlifyBindApi.get().registerBinding { it.apply {
        id("zoomify", "secondary_zoom")
        allowedContexts(BindContext.IN_GAME)
        addKeyCorrelation(Zoomify.secondaryZoomKeyMapping)
        radialCandidate(RadialIcons.getItem(Items.SPYGLASS))
        category(category)
        name(Component.translatable(Zoomify.secondaryZoomKeyMapping.name))
    } }

    override fun onControlifyPreInit(controlify: ControlifyApi) {
        ControlifyEvents.LOOK_INPUT_MODIFIER.register { event ->
            event.lookInput.x /= Mth.lerp(ZoomifySettings.relativeSensitivity / 100.0, 1.0, Zoomify.previousZoomDivisor).toFloat()
            event.lookInput.y /= Mth.lerp(ZoomifySettings.relativeSensitivity / 100.0, 1.0, Zoomify.previousZoomDivisor).toFloat()
        }

        Zoomify.zoomHoldKey.addSource(InputBindingKeySource(ZOOM_HOLD))
        Zoomify.zoomToggleKey.addSource(InputBindingKeySource(ZOOM_TOGGLE))
        Zoomify.secondaryZoomKey.addSource(InputBindingKeySource(SECONDARY_ZOOM))
    }

    override fun onControllersDiscovered(controlify: ControlifyApi) {

    }
}

private class InputBindingKeySource(private val bindingSupplier: InputBindingSupplier) : KeySource {
    private val binding: InputBinding?
        get() = ControlifyApi.get().currentController.orElse(null)?.let { bindingSupplier.on(it) }

    override val justPressed: Boolean
        get() = binding?.justPressed() == true

    override val isDown: Boolean
        get() = binding?.digitalNow() == true
}
