package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.Zoomify
import net.minecraft.util.Mth
import kotlin.math.pow

class ZoomHelper(
    private val initialInterpolator: Interpolator,
    private val scrollInterpolator: Interpolator,

    private val initialZoom: () -> Int,
    private val scrollZoomAmount: () -> Int,
    val maxScrollTiers: () -> Int,
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
        initialInterpolation =
            initialInterpolator.tickInterpolation(targetZoom, initialInterpolation, lastFrameDuration)
        prevInitialInterpolation = initialInterpolator.modifyPrevInterpolation(prevInitialInterpolation)
        if (!initialInterpolator.isSmooth)
            prevInitialInterpolation = initialInterpolation
        zoomingLastTick = zooming
    }

    private fun tickScroll(scrollTiers: Int, lastFrameDuration: Double) {
        if (scrollTiers > lastScrollTier)
            resetting = false

        val targetZoom =
            if (maxScrollTiers() > 0)
                scrollTiers.toDouble() / Zoomify.maxScrollTiers
            else 0.0

        prevScrollInterpolation = scrollInterpolation
        scrollInterpolation = scrollInterpolator.tickInterpolation(targetZoom, scrollInterpolation, lastFrameDuration)
        prevScrollInterpolation = scrollInterpolator.modifyPrevInterpolation(prevScrollInterpolation)
        if (!initialInterpolator.isSmooth)
            prevInitialInterpolation = initialInterpolation
        lastScrollTier = scrollTiers
    }

    fun getZoomDivisor(tickDelta: Float = 1f): Double {
        val initialMultiplier = getInitialZoomMultiplier(tickDelta)
        val baseDivisor = 1 / initialMultiplier

        // Get the smoothed scroll interpolation value (0 to 1)
        val scrollT = if (resetting) 0.0 else {
            if (scrollInterpolator.isSmooth) scrollInterpolator.modifyInterpolation(
                Mth.lerp(
                    tickDelta.toDouble(),
                    prevScrollInterpolation,
                    scrollInterpolation
                )
            ) else scrollInterpolation
        }

        val maxScrollDivisor = Zoomify.maxScrollTiers * (scrollZoomAmount() * 3.0)

        val finalDivisor = if (linearLikeSteps() && scrollT > 0 && maxScrollDivisor > 0) {
            // Geometric interpolation for perceptually uniform zoom steps
            // Each scroll step produces a constant multiplicative change to the divisor
            // scrollZoomAmount scales how fast you reach max zoom:
            // - scrollZoomAmount=1: reach max at step 30 (subtle, ~15% per step)
            // - scrollZoomAmount=3: reach max at step 10 (moderate, ~52% per step)
            // - scrollZoomAmount=10: reach max at step 3 (aggressive, ~300% per step)
            val effectiveT = (scrollT * scrollZoomAmount()).coerceAtMost(1.0)
            val maxDivisor = baseDivisor + maxScrollDivisor
            baseDivisor.pow(1 - effectiveT) * maxDivisor.pow(effectiveT)
        } else {
            // Linear interpolation (original behavior)
            baseDivisor + scrollT * maxScrollDivisor
        }

        return finalDivisor.also {
            if (initialInterpolation == 0.0 && scrollInterpolation == 0.0) resetting = false
            if (!resetting) resetMultiplier = 1 / it
        }
    }

    private fun getInitialZoomMultiplier(tickDelta: Float): Double {
        return Mth.lerp(
            if (initialInterpolator.isSmooth) initialInterpolator.modifyInterpolation(
                Mth.lerp(
                    tickDelta.toDouble(),
                    prevInitialInterpolation,
                    initialInterpolation
                )
            ) else initialInterpolation,
            1.0,
            if (!resetting) 1 / initialZoom().toDouble() else resetMultiplier
        )
    }

    fun reset() {
        if (!resetting && scrollInterpolation > 0.0) {
            resetting = true
            scrollInterpolation = 0.0
            prevScrollInterpolation = 0.0
        }
    }

    fun setToZero(initial: Boolean = true, scroll: Boolean = true) {
        if (initial) {
            initialInterpolation = 0.0
            prevInitialInterpolation = 0.0
            zoomingLastTick = false
        }
        if (scroll) {
            scrollInterpolation = 0.0
            prevScrollInterpolation = 0.0
            lastScrollTier = 0
        }
        resetting = false
    }

    fun skipInitial() {
        initialInterpolation = 1.0
        prevInitialInterpolation = 1.0
    }
}
