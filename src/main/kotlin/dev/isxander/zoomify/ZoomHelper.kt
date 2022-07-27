package dev.isxander.zoomify

import dev.isxander.zoomify.config.ZoomifySettings
import dev.isxander.zoomify.utils.TransitionType
import net.minecraft.util.math.MathHelper

class ZoomHelper(private val starting: Double = 1.0) {
    private var prevInitialInterpolation = 0.0
    private var initialInterpolation = 0.0

    private var zoomingLastTick = false
    private var activeTransition = ZoomifySettings.zoomTransition

    private var prevScrollInterpolation = 0.0
    private var scrollInterpolation = 0.0
    private var lastScrollTier = 0

    private var resetting = false
    private var resetInterpolation = 0.0

    fun tick(zooming: Boolean, scrollTiers: Int) {
        tickInitial(zooming)
        tickScroll(scrollTiers)
    }

    private fun tickInitial(zooming: Boolean) {
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

            initialInterpolation += 0.05 / ZoomifySettings.zoomInTime
            initialInterpolation = initialInterpolation.coerceAtMost(targetZoom)
        } else if (targetZoom < initialInterpolation) {
            if (ZoomifySettings.zoomOppositeTransitionOut) {
                activeTransition = activeTransition.opposite()
                if (zoomingLastTick && activeTransition.hasInverse()) {
                    prevInitialInterpolation = activeTransition.inverse(transition.apply(prevInitialInterpolation))
                    initialInterpolation = activeTransition.inverse(transition.apply(initialInterpolation))
                }
            }

            initialInterpolation -= 0.05 / ZoomifySettings.zoomOutTime
            initialInterpolation = initialInterpolation.coerceAtLeast(targetZoom)
        }

        zoomingLastTick = zooming
    }

    private fun tickScroll(scrollTiers: Int) {
        if (scrollTiers > lastScrollTier)
            resetting = false

        val targetZoom = scrollTiers.toDouble() / Zoomify.maxScrollTiers

        prevScrollInterpolation = scrollInterpolation

        val smoothness = MathHelper.lerp(ZoomifySettings.scrollZoomSmoothness / 100.0, 1.0, 0.1)
        if (scrollInterpolation < targetZoom) {
            scrollInterpolation += (targetZoom - scrollInterpolation) * smoothness
            scrollInterpolation = scrollInterpolation.coerceAtMost(targetZoom)
        } else if (scrollInterpolation > targetZoom) {
            scrollInterpolation -= (scrollInterpolation - targetZoom) * smoothness
            scrollInterpolation = scrollInterpolation.coerceAtLeast(targetZoom)
        }

        lastScrollTier = scrollTiers
    }

    fun getZoomDivisor(tickDelta: Float): Double {
        val initialDivisor = getInitialZoomDivisor(tickDelta)
        val scrollDivisor = getScrollZoomDivisor(tickDelta)

        return (initialDivisor + scrollDivisor).also {
            if (!resetting) resetInterpolation = it
            if (it == 1.0) resetting = false
        }
    }

    private fun getInitialZoomDivisor(tickDelta: Float): Double {
        return MathHelper.lerp(
            activeTransition.apply(MathHelper.lerp(tickDelta.toDouble(), prevInitialInterpolation, initialInterpolation)),
            starting,
            if (!resetting) ZoomifySettings.initialZoom.toDouble() else resetInterpolation
        )
    }

    private fun getScrollZoomDivisor(tickDelta: Float): Double {
        return MathHelper.lerp(
            MathHelper.lerp(tickDelta.toDouble(), prevScrollInterpolation, scrollInterpolation),
            0.0,
            Zoomify.maxScrollTiers * ZoomifySettings.scrollZoomAmount.toDouble()
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
