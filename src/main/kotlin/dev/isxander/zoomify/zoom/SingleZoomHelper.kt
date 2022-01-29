package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.utils.TransitionType
import dev.isxander.zoomify.utils.lerp

open class SingleZoomHelper(private val _initialZoom: () -> Double, zoomSpeed: () -> Double, transition: () -> TransitionType) : ZoomHelper<SingleZoomHelper.SingleZoomParams>(zoomSpeed, transition) {
    val initialZoom: Double
        get() = _initialZoom()

    private var prevZoomDivisor = 0.0
    private var ticks = 0.0

    override fun getZoomDivisor(params: SingleZoomParams): Double {
        val zooming = params.zooming
        val tickDelta = params.tickDelta

        val targetZoom = if (zooming) 1.0 else 0.0

        if (transition == TransitionType.INSTANT) {
            prevZoomDivisor = targetZoom
        } else if (targetZoom > prevZoomDivisor) {
            prevZoomDivisor += zoomSpeed / 20 * tickDelta
            prevZoomDivisor = prevZoomDivisor.coerceAtMost(targetZoom)
        } else if (targetZoom < prevZoomDivisor) {
            prevZoomDivisor -= tickDelta * (zoomSpeed / 20)
            prevZoomDivisor = prevZoomDivisor.coerceAtLeast(targetZoom)
        }

        ticks++
        return lerp(1.0, initialZoom, transition.takeUnless { it == TransitionType.INSTANT }?.apply(prevZoomDivisor) ?: prevZoomDivisor)
    }

    data class SingleZoomParams(val zooming: Boolean, val tickDelta: Float) : ZoomParams()
}
