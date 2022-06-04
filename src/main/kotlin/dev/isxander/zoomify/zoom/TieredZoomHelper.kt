package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.utils.TransitionType
import net.minecraft.util.math.MathHelper

class TieredZoomHelper(
    zoomSpeed: () -> Double,
    transition: () -> TransitionType,
    private val _maxTiers: () -> Int,
    private val _maxZoom: () -> Double
) : ZoomHelper(zoomSpeed, transition) {
    val maxTiers: Int
        get() = _maxTiers()

    val maxZoom: Double
        get() = _maxZoom()

    private var interpolation = 0.0

    private var lastTier = 0

    fun getZoomDivisor(tier: Int, tickDelta: Float): Double {
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
                    interpolation = actualTransition.inverse(transition.apply(interpolation))
                }
            }

            interpolation -= zoomSpeed / 20 * tickDelta
            interpolation = interpolation.coerceAtLeast(targetZoom)
        }

        return MathHelper.lerp(
            actualTransition.apply(interpolation),
            0.0,
            maxZoom,
        ).also { lastTier = tier }
    }
}
