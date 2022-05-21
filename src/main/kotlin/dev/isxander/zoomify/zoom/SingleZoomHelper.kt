package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.utils.TransitionType
import dev.isxander.zoomify.utils.lerp

open class SingleZoomHelper(private val _initialZoom: () -> Double, zoomSpeed: () -> Double, transition: () -> TransitionType) : ZoomHelper<SingleZoomHelper.SingleZoomParams>(zoomSpeed, transition) {
    val initialZoom: Double
        get() = _initialZoom()

    private var interpolation = 0.0

    override fun getZoomDivisor(params: SingleZoomParams): Double {
        val zooming = params.zooming
        val tickDelta = params.tickDelta

        val targetZoom = if (zooming) 1.0 else 0.0
        var actualTransition = transition

        if (transition == TransitionType.INSTANT) {
            interpolation = targetZoom
        } else if (targetZoom > interpolation) {
            interpolation += zoomSpeed / 20 * 0.05 + tickDelta
            interpolation = interpolation.coerceAtMost(targetZoom)
        } else if (targetZoom < interpolation) {
            interpolation -= zoomSpeed / 20 * 0.05 + tickDelta
            interpolation = interpolation.coerceAtLeast(targetZoom)

            if (ZoomifySettings.zoomOppositeTransitionOut)
                actualTransition = actualTransition.opposite()
        }

        return lerp(1.0, initialZoom, actualTransition.takeUnless { it == TransitionType.INSTANT }?.apply(interpolation) ?: interpolation)
    }

    data class SingleZoomParams(val zooming: Boolean, val tickDelta: Float) : ZoomParams()
}
