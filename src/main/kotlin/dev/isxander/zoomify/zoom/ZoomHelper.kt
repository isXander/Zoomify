package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.Zoomify
import net.minecraft.util.Mth
import kotlin.math.pow

class ZoomHelper(
    private val initialInterpolator: Interpolator,
    private val scrollInterpolator: Interpolator,

    private val initialZoom: () -> Int,
    private val zoomPerStep: () -> Int,
    val maxScrollTiers: () -> Int,
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

        // scrollTiers can be negative (zoom out) or positive (zoom in)
        // Normalize to -1..1 range where 0 is initial zoom
        val targetZoom =
            if (maxScrollTiers() > 0)
                scrollTiers.toDouble() / maxScrollTiers()
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

        // Get the smoothed scroll interpolation value (-1 to 1)
        // Negative = zoom out, Positive = zoom in
        val scrollT = if (resetting) 0.0 else {
            if (scrollInterpolator.isSmooth) scrollInterpolator.modifyInterpolation(
                Mth.lerp(
                    tickDelta.toDouble(),
                    prevScrollInterpolation,
                    scrollInterpolation
                )
            ) else scrollInterpolation
        }

        // zoomPerStep is stored as percentage (e.g., 150 = 1.5x per step)
        val stepMultiplier = zoomPerStep() / 100.0
        val maxSteps = maxScrollTiers()
        // Current step number (interpolated for smooth animation)
        // Can be negative for zooming out below initial zoom
        val currentStep = scrollT * maxSteps

        // Geometric interpolation for perceptually uniform zoom steps
        // Each step multiplies the divisor by stepMultiplier
        // divisor = baseDivisor × stepMultiplier^currentStep
        // When currentStep is negative, this zooms out (divisor < baseDivisor)
        val rawDivisor = baseDivisor * stepMultiplier.pow(currentStep)

        // Safety limits to prevent rendering issues
        // Min 0.5x zoom (divisor 0.5), max 500x zoom (divisor 500)
        val finalDivisor = rawDivisor.coerceIn(0.5, 500.0)

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
