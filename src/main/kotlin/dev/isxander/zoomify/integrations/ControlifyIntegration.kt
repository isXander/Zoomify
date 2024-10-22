package dev.isxander.zoomify.integrations

import dev.isxander.controlify.api.ControlifyApi
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint
import dev.isxander.controlify.api.event.ControlifyEvents
import dev.isxander.yacl3.config.v3.value
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.config.ZoomifySettings
import net.minecraft.util.Mth

object ControlifyIntegration : ControlifyEntrypoint {
    override fun onControlifyInit(controlify: ControlifyApi) {
        ControlifyEvents.LOOK_INPUT_MODIFIER.register {
            it.lookInput.x /= Mth.lerp(ZoomifySettings.relativeSensitivity.value / 100.0, 1.0, Zoomify.previousZoomDivisor).toFloat()
            it.lookInput.y /= Mth.lerp(ZoomifySettings.relativeSensitivity.value / 100.0, 1.0, Zoomify.previousZoomDivisor).toFloat()
        }
    }

    override fun onControllersDiscovered(controlify: ControlifyApi) {

    }
}
