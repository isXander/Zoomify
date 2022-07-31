package dev.isxander.zoomify

import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.utils.TransitionType
import net.minecraft.util.math.MathHelper
import kotlin.math.pow

class ZoomHelper(private val starting: Double = 1.0) {
    private var prevInitialInterpolation = 0.0
    private var initialInterpolation = 0.0

    private var zoomingLastTick = false
    private lateinit var activeTransition: TransitionType

    private var prevScrollInterpolation = 0.0
    private var scrollInterpolation = 0.0
    private var lastScrollTier = 0

    private var resetting = false
    private var resetInterpolation = 0.0

    fun tick(zooming: Boolean, scrollTiers: Int, lastFrameDuration: Double = 0.05) {
        tickInitial(zooming, lastFrameDuration)
        tickScroll(scrollTiers, lastFrameDuration)
    }

    private fun tickInitial(zooming: Boolean, lastFrameDuration: Double) {
        if (zooming && !zoomingLastTick)
            resetting = false

        val targetZoom = if (zooming) 1.0 else 0.0
        val transition = ZoomifySettings.zoomTransition
        activeTransition = ZoomifySettings.zoomTransition
        prevInitialInterpolation = initialInterpolation

        if (activeTransition == TransitionType.INSTANT) {
            initialInterpolation = targetZoom
        } else if (targetZoom > initialInterpolation) {
            activeTransition = transition

            if (ZoomifySettings.zoomOppositeTransitionOut && !zoomingLastTick && transition.hasInverse()) {
                prevInitialInterpolation = transition.inverse(transition.opposite().apply(prevInitialInterpolation))
                initialInterpolation = transition.inverse(transition.opposite().apply(initialInterpolation))
            }

            initialInterpolation += lastFrameDuration / ZoomifySettings.zoomInTime
            initialInterpolation = initialInterpolation.coerceAtMost(targetZoom)
        } else if (targetZoom < initialInterpolation) {
            if (ZoomifySettings.zoomOppositeTransitionOut) {
                activeTransition = activeTransition.opposite()
                if (zoomingLastTick && activeTransition.hasInverse()) {
                    prevInitialInterpolation = activeTransition.inverse(transition.apply(prevInitialInterpolation))
                    initialInterpolation = activeTransition.inverse(transition.apply(initialInterpolation))
                }
            }

            initialInterpolation -= lastFrameDuration / ZoomifySettings.zoomOutTime
            initialInterpolation = initialInterpolation.coerceAtLeast(targetZoom)
        }

        zoomingLastTick = zooming
    }

    private fun tickScroll(scrollTiers: Int, lastFrameDuration: Double) {
        if (scrollTiers > lastScrollTier)
            resetting = false

        var targetZoom = scrollTiers.toDouble() / Zoomify.maxScrollTiers
        val curvature = 0.3
        val exp = 1 / (1 - curvature)
        targetZoom = 2 * (targetZoom.pow(exp) / (targetZoom.pow(exp) + (2 - targetZoom).pow(exp)))

        prevScrollInterpolation = scrollInterpolation

        val smoothness = MathHelper.lerp(ZoomifySettings.scrollZoomSmoothness / 100.0, 1.0, 0.1)
        if (scrollInterpolation < targetZoom) {
            scrollInterpolation += (targetZoom - scrollInterpolation) * smoothness / 0.05 * lastFrameDuration
            scrollInterpolation = scrollInterpolation.coerceAtMost(targetZoom)
        } else if (scrollInterpolation > targetZoom) {
            scrollInterpolation -= (scrollInterpolation - targetZoom) * smoothness / 0.05 * lastFrameDuration
            scrollInterpolation = scrollInterpolation.coerceAtLeast(targetZoom)
        }

        lastScrollTier = scrollTiers
    }

    fun getZoomDivisor(tickDelta: Float = 1f): Double {
        val initialMultiplier = getInitialZoomMultiplier(tickDelta)
        val scrollDivisor = getScrollZoomDivisor(tickDelta)

        return (1/initialMultiplier + scrollDivisor).also {
            if (initialInterpolation == 0.0 && scrollInterpolation == 0.0) resetting = false
            if (!resetting) resetInterpolation = 1/it
        }
    }

    private fun getInitialZoomMultiplier(tickDelta: Float): Double {
        return MathHelper.lerp(
            activeTransition.apply(MathHelper.lerp(tickDelta.toDouble(), prevInitialInterpolation, initialInterpolation)),
            1 / starting,
            if (!resetting) 1 / ZoomifySettings.initialZoom.toDouble() else resetInterpolation
        )
    }

    private fun getScrollZoomDivisor(tickDelta: Float): Double {
        return MathHelper.lerp(
            MathHelper.lerp(tickDelta.toDouble(), prevScrollInterpolation, scrollInterpolation),
            0.0,
            Zoomify.maxScrollTiers * (ZoomifySettings.scrollZoomAmount * 2.0)
        ).let { if (resetting) 0.0 else it }
    }

    fun reset() {
        if (scrollInterpolation > 0.0) {
            resetting = true
            scrollInterpolation = 0.0
            prevScrollInterpolation = 0.0
        }
    }
}
