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

        // zoomPerStep is stored as percentage (e.g., 150 = 1.5x per step)
        val stepMultiplier = zoomPerStep() / 100.0
        val maxSteps = maxScrollTiers()
        // Current step number (interpolated for smooth animation)
        val currentStep = scrollT * maxSteps

        val finalDivisor = if (linearLikeSteps() && scrollT > 0 && maxSteps > 0) {
            // Geometric interpolation for perceptually uniform zoom steps
            // Each step multiplies the divisor by stepMultiplier
            // divisor = baseDivisor × stepMultiplier^currentStep
            baseDivisor * stepMultiplier.pow(currentStep)
        } else {
            // Linear interpolation (non-uniform perception, legacy behavior)
            // Same endpoints but linear interpolation between them
            val maxDivisor = baseDivisor * stepMultiplier.pow(maxSteps.toDouble())
            Mth.lerp(scrollT, baseDivisor, maxDivisor)
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
