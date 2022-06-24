package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.utils.TransitionType
import net.minecraft.util.math.MathHelper

class SingleZoomHelper(private val starting: Double = 1.0) {
    private val zoomSpeed: Double
        get() = ZoomifySettings.zoomSpeed / 100.0

    private var interpolation = 0.0
    private var zoomingLastTick = false

    fun getZoomDivisor(zooming: Boolean, tickDelta: Float): Double {
        val targetZoom = if (zooming) 1.0 else 0.0
        val transition = ZoomifySettings.zoomTransition
        var actualTransition = ZoomifySettings.zoomTransition

        if (actualTransition == TransitionType.INSTANT) {
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
                    interpolation = actualTransition.inverse(transition.apply(interpolation))
                }
            }

            interpolation -= zoomSpeed / 20 * tickDelta
            interpolation = interpolation.coerceAtLeast(targetZoom)
        }

        return MathHelper.lerp(
            actualTransition.apply(interpolation),
            starting,
            ZoomifySettings.initialZoom.toDouble()
        ).also { zoomingLastTick = zooming }
    }
}
