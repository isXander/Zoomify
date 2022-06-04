package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.utils.TransitionType
import dev.isxander.zoomify.utils.lerp

open class TieredZoomHelper(zoomSpeed: () -> Double, transition: () -> TransitionType, private val _maxTiers: () -> Int, private val _maxZoom: () -> Double) : ZoomHelper<TieredZoomHelper.TieredZoomParams>(zoomSpeed, transition) {
    val maxTiers: Int
        get() = _maxTiers()

    val maxZoom: Double
        get() = _maxZoom()

    private var interpolation = 0.0

    private var lastTier = 0

    override fun getZoomDivisor(params: TieredZoomParams): Double {
        val tier = params.tier
        val tickDelta = params.tickDelta

        val targetZoom = tier.toDouble() / maxTiers
        var actualTransition = transition
        val zoomingInLastTick = tier > lastTier

        if (transition == TransitionType.INSTANT) {
            interpolation = targetZoom
        } else if (targetZoom > interpolation) {
            if (ZoomifySettings.scrollZoomOppositeTransitionOut && !zoomingInLastTick && transition.hasInverse()) {
                interpolation = transition.inverse(transition.opposite().apply(interpolation))
            }

            interpolation += zoomSpeed / 20 * tickDelta
            interpolation = interpolation.coerceAtMost(targetZoom)
        } else if (targetZoom < interpolation) {
            if (ZoomifySettings.scrollZoomOppositeTransitionOut) {
                actualTransition = actualTransition.opposite()
                if (zoomingInLastTick && actualTransition.hasInverse()) {
                    // find what the interpolation would be in the opposite transition
                    interpolation = actualTransition.inverse(transition.apply(interpolation))
                }
            }

            interpolation -= zoomSpeed / 20 * tickDelta
            interpolation = interpolation.coerceAtLeast(targetZoom)
        }

        return lerp(0.0, maxZoom, actualTransition.takeUnless { it == TransitionType.INSTANT }?.apply(interpolation) ?: interpolation).also {
            lastTier = tier
        }
    }

    data class TieredZoomParams(val tier: Int, val tickDelta: Float) : ZoomParams()
}
