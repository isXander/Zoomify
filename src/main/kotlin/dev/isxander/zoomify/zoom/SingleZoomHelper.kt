package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.utils.TransitionType
import dev.isxander.zoomify.utils.lerp

open class SingleZoomHelper(private val _initialZoom: () -> Double, zoomSpeed: () -> Double, transition: () -> TransitionType) : ZoomHelper<SingleZoomHelper.SingleZoomParams>(zoomSpeed, transition) {
    val initialZoom: Double
        get() = _initialZoom()

    private var interpolation = 0.0
    private var zoomingLastTick = false

    override fun getZoomDivisor(params: SingleZoomParams): Double {
        val zooming = params.zooming
        val tickDelta = params.tickDelta

        val targetZoom = if (zooming) 1.0 else 0.0
        var actualTransition = transition

        if (transition == TransitionType.INSTANT) {
            interpolation = targetZoom
        } else if (targetZoom > interpolation) {
            if (ZoomifySettings.zoomOppositeTransitionOut && !zoomingLastTick && transition.hasInverse()) {
                interpolation = transition.inverse(transition.opposite().apply(interpolation))
            }

            interpolation += zoomSpeed / 20 * tickDelta
            interpolation = interpolation.coerceAtMost(targetZoom)
        } else if (targetZoom < interpolation) {
            if (ZoomifySettings.zoomOppositeTransitionOut) {
                actualTransition = actualTransition.opposite()
                if (zoomingLastTick && actualTransition.hasInverse()) {
                    // find what the interpolation would be in the opposite transition
                    interpolation = actualTransition.inverse(transition.apply(interpolation))
                }
            }

            interpolation -= zoomSpeed / 20 * tickDelta
            interpolation = interpolation.coerceAtLeast(targetZoom)
        }

        return lerp(1.0, initialZoom, actualTransition.takeUnless { it == TransitionType.INSTANT }?.apply(interpolation) ?: interpolation).also { zoomingLastTick = zooming }
    }

    data class SingleZoomParams(val zooming: Boolean, val tickDelta: Float) : ZoomParams()
}
