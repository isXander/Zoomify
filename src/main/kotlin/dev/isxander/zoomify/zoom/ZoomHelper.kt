package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.config.ZoomifySettings
import net.minecraft.util.math.MathHelper
import kotlin.math.pow

class ZoomHelper(
    private val initialInterpolator: Interpolator,
    private val scrollInterpolator: Interpolator,

    private val initialZoom: () -> Int,
    private val scrollZoomAmount: () -> Int,
    private val maxScrollTiers: () -> Int,
    private val linearLikeSteps: () -> Boolean,
) {
    private var prevInitialInterpolation = 0.0
    private var initialInterpolation = 0.0

    private var zoomingLastTick = false

    private var prevScrollInterpolation = 0.0
    private var scrollInterpolation = 0.0
    private var lastScrollTier = 0

    private var resetting = false
    private var resetMultiplier = 0.0

    fun tick(zooming: Boolean, scrollTiers: Int, lastFrameDuration: Double = 0.05) {
        tickInitial(zooming, lastFrameDuration)
        tickScroll(scrollTiers, lastFrameDuration)
    }

    private fun tickInitial(zooming: Boolean, lastFrameDuration: Double) {
        if (zooming && !zoomingLastTick)
            resetting = false

        val targetZoom = if (zooming) 1.0 else 0.0
        prevInitialInterpolation = initialInterpolation
        initialInterpolation = initialInterpolator.tickInterpolation(targetZoom, initialInterpolation, lastFrameDuration)
        prevInitialInterpolation = initialInterpolator.modifyPrevInterpolation(prevInitialInterpolation)
        if (!initialInterpolator.isSmooth)
            prevInitialInterpolation = initialInterpolation
        zoomingLastTick = zooming
    }

    private fun tickScroll(scrollTiers: Int, lastFrameDuration: Double) {
        if (scrollTiers > lastScrollTier)
            resetting = false

        var targetZoom =
            if (maxScrollTiers() > 0)
                scrollTiers.toDouble() / Zoomify.maxScrollTiers
            else 0.0
        if (linearLikeSteps()) {
            val curvature = 0.3
            val exp = 1 / (1 - curvature)
            targetZoom = 2 * (targetZoom.pow(exp) / (targetZoom.pow(exp) + (2 - targetZoom).pow(exp)))
        }

        prevScrollInterpolation = scrollInterpolation
        scrollInterpolation = scrollInterpolator.tickInterpolation(targetZoom, scrollInterpolation, lastFrameDuration)
        prevScrollInterpolation = scrollInterpolator.modifyPrevInterpolation(prevScrollInterpolation)
        if (!initialInterpolator.isSmooth)
            prevInitialInterpolation = initialInterpolation
        lastScrollTier = scrollTiers
    }

    fun getZoomDivisor(tickDelta: Float = 1f): Double {
        val initialMultiplier = getInitialZoomMultiplier(tickDelta)
        val scrollDivisor = getScrollZoomDivisor(tickDelta)

        return (1 / initialMultiplier + scrollDivisor).also {
            if (initialInterpolation == 0.0 && scrollInterpolation == 0.0) resetting = false
            if (!resetting) resetMultiplier = 1 / it
        }
    }

    private fun getInitialZoomMultiplier(tickDelta: Float): Double {
        return MathHelper.lerp(
            initialInterpolator.modifyInterpolation(MathHelper.lerp(tickDelta.toDouble(), prevInitialInterpolation, initialInterpolation)),
            1.0,
            if (!resetting) 1 / initialZoom().toDouble() else resetMultiplier
        )
    }

    private fun getScrollZoomDivisor(tickDelta: Float): Double {
        return MathHelper.lerp(
            scrollInterpolator.modifyInterpolation(MathHelper.lerp(tickDelta.toDouble(), prevScrollInterpolation, scrollInterpolation)),
            0.0,
            Zoomify.maxScrollTiers * (scrollZoomAmount() * 3.0)
        ).let { if (resetting) 0.0 else it }
    }

    fun reset() {
        if (!resetting && scrollInterpolation > 0.0) {
            resetting = true
            scrollInterpolation = 0.0
            prevScrollInterpolation = 0.0
        }
    }
}
