package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.utils.TransitionType

abstract class ZoomHelper<T : ZoomHelper.ZoomParams>(private val _zoomSpeed: () -> Double, private val _transition: () -> TransitionType) {
    val zoomSpeed: Double
        get() = _zoomSpeed()

    val transition: TransitionType
        get() = _transition()

    abstract fun getZoomDivisor(params: T): Double

    abstract class ZoomParams
}
