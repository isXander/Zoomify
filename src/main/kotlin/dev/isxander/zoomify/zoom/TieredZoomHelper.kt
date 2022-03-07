package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.utils.TransitionType
import dev.isxander.zoomify.utils.lerp

open class TieredZoomHelper(zoomSpeed: () -> Double, transition: () -> TransitionType, private val _maxTiers: () -> Int, private val _maxZoom: () -> Double) : ZoomHelper<TieredZoomHelper.TieredZoomParams>(zoomSpeed, transition) {
    val maxTiers: Int
        get() = _maxTiers()

    val maxZoom: Double
        get() = _maxZoom()

    private var prevZoomDivisor = 0.0

    override fun getZoomDivisor(params: TieredZoomParams): Double {
        val tier = params.tier
        val tickDelta = params.tickDelta

        val targetZoom = tier.toDouble() / maxTiers
        var actualTransition = transition

        if (transition == TransitionType.INSTANT) {
            prevZoomDivisor = targetZoom
        } else if (targetZoom > prevZoomDivisor) {
            prevZoomDivisor += tickDelta * (zoomSpeed / 20)
            prevZoomDivisor = prevZoomDivisor.coerceAtMost(targetZoom)
        } else if (targetZoom < prevZoomDivisor) {
            prevZoomDivisor -= tickDelta * (zoomSpeed / 20)
            prevZoomDivisor = prevZoomDivisor.coerceAtLeast(targetZoom)

            if (ZoomifySettings.scrollZoomOppositeTransitionOut)
                actualTransition = actualTransition.opposite()
        }

        return lerp(0.0, maxZoom, actualTransition.takeUnless { it == TransitionType.INSTANT }?.apply(prevZoomDivisor) ?: prevZoomDivisor)
    }

    data class TieredZoomParams(val tier: Int, val tickDelta: Float) : ZoomParams()
}
