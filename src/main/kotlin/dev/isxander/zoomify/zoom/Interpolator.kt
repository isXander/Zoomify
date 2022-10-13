package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.utils.TransitionType
import kotlin.math.max
import kotlin.math.min

interface Interpolator {
    fun tickInterpolation(targetInterpolation: Double, currentInterpolation: Double, tickDelta: Double): Double

    fun modifyInterpolation(interpolation: Double): Double = interpolation

    fun modifyPrevInterpolation(interpolation: Double): Double = interpolation
    val isSmooth: Boolean
}

object InstantInterpolator : Interpolator {
    override fun tickInterpolation(targetInterpolation: Double, currentInterpolation: Double, tickDelta: Double): Double {
        return targetInterpolation
    }

    override val isSmooth: Boolean = false
}

sealed class LinearInterpolator : Interpolator {
    protected var goingIn = true
        private set

    override fun tickInterpolation(targetInterpolation: Double, currentInterpolation: Double, tickDelta: Double): Double {
        if (targetInterpolation > currentInterpolation) {
            goingIn = true
            return min(currentInterpolation + getTimeIncrement(false, tickDelta, targetInterpolation, currentInterpolation), targetInterpolation)
        } else if (targetInterpolation < currentInterpolation) {
            goingIn = false
            return max(currentInterpolation - getTimeIncrement(true, tickDelta, targetInterpolation, currentInterpolation), targetInterpolation)
        }
        goingIn = true
        return targetInterpolation
    }

    override val isSmooth: Boolean = true

    abstract fun getTimeIncrement(
        zoomingOut: Boolean,
        tickDelta: Double,
        targetInterpolation: Double,
        currentInterpolation: Double
    ): Double
}

open class TimedInterpolator(val timeIn: () -> Double, val timeOut: () -> Double) : LinearInterpolator() {
    override fun getTimeIncrement(
        zoomingOut: Boolean,
        tickDelta: Double,
        targetInterpolation: Double,
        currentInterpolation: Double
    ): Double {
        return tickDelta / if (zoomingOut) timeOut() else timeIn()
    }

    override val isSmooth: Boolean
        get() = (goingIn && timeIn() > 0.0) || (!goingIn && timeOut() > 0.0)
}

class TransitionInterpolator(val transitionIn: () -> TransitionType, val transitionOut: () -> TransitionType, timeIn: () -> Double, timeOut: () -> Double) : TimedInterpolator(timeIn, timeOut) {
    private var activeTransition: TransitionType = transitionIn()
    private var inactiveTransition: TransitionType = transitionOut()
    private var prevTargetInterpolation: Double = 0.0
    private var justSwappedTransition = false

    override fun tickInterpolation(
        targetInterpolation: Double,
        currentInterpolation: Double,
        tickDelta: Double
    ): Double {
        var currentInterpolationMod = currentInterpolation

        if (targetInterpolation > currentInterpolation) {
            activeTransition = transitionIn()
            inactiveTransition = transitionOut()

            if (prevTargetInterpolation < targetInterpolation && activeTransition.hasInverse()) {
                justSwappedTransition = true
                currentInterpolationMod = activeTransition.inverse(inactiveTransition.apply(currentInterpolationMod))
            }
        } else if (targetInterpolation < currentInterpolation) {
            activeTransition = transitionOut()
            inactiveTransition = transitionIn()

            if (prevTargetInterpolation > targetInterpolation && activeTransition.hasInverse()) {
                justSwappedTransition = true
                currentInterpolationMod = activeTransition.inverse(inactiveTransition.apply(currentInterpolationMod))
            }
        }

        prevTargetInterpolation = targetInterpolation

        if (activeTransition == TransitionType.INSTANT)
            return targetInterpolation

        return super.tickInterpolation(targetInterpolation, currentInterpolationMod, tickDelta)
    }

    override fun modifyInterpolation(interpolation: Double): Double {
        return activeTransition.apply(interpolation)
    }

    override fun modifyPrevInterpolation(interpolation: Double): Double {
        if (justSwappedTransition) {
            justSwappedTransition = false
            return activeTransition.inverse(inactiveTransition.apply(interpolation))
        }
        return interpolation
    }

    override val isSmooth: Boolean
        get() = !justSwappedTransition && super.isSmooth
}

class SmoothInterpolator(val smoothness: () -> Double) : LinearInterpolator() {
    override fun getTimeIncrement(
        zoomingOut: Boolean,
        tickDelta: Double,
        targetInterpolation: Double,
        currentInterpolation: Double
    ): Double {
        val diff = if (!zoomingOut) targetInterpolation - currentInterpolation else currentInterpolation - targetInterpolation
        return diff * smoothness() / 0.05 * tickDelta
    }

    override fun tickInterpolation(
        targetInterpolation: Double,
        currentInterpolation: Double,
        tickDelta: Double
    ): Double {
        if (!isSmooth)
            return targetInterpolation

        return super.tickInterpolation(targetInterpolation, currentInterpolation, tickDelta)
    }

    override val isSmooth: Boolean
        get() = smoothness() != 1.0
}
