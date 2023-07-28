package dev.isxander.zoomify.integrations

//import dev.isxander.controlify.api.ControlifyApi
//import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint
//import dev.isxander.controlify.api.event.ControlifyEvents
//import dev.isxander.controlify.api.ingameinput.LookInputModifier
//import dev.isxander.controlify.controller.Controller
//import dev.isxander.zoomify.Zoomify
//import dev.isxander.zoomify.config.ZoomifySettings
//import net.minecraft.util.math.MathHelper
//
//object ControlifyIntegration : ControlifyEntrypoint {
//    override fun onControlifyPreInit(controlify: ControlifyApi) {
//        ControlifyEvents.LOOK_INPUT_MODIFIER.register(SensitivityModifier)
//    }
//
//    override fun onControllersDiscovered(controlify: ControlifyApi) {
//
//    }
//
//    object SensitivityModifier : LookInputModifier {
//        override fun modifyX(x: Float, controller: Controller<*, *>): Float {
//            return x * MathHelper.lerp(ZoomifySettings.relativeSensitivity / 100.0, 1.0, Zoomify.previousZoomDivisor).toFloat()
//        }
//
//        override fun modifyY(y: Float, controller: Controller<*, *>): Float {
//            return y * MathHelper.lerp(ZoomifySettings.relativeSensitivity / 100.0, 1.0, Zoomify.previousZoomDivisor).toFloat()
//        }
//    }
//}
