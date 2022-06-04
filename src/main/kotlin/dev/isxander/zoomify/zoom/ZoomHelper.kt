package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.utils.TransitionType

abstract class ZoomHelper(private val _zoomSpeed: () -> Double, private val _transition: () -> TransitionType) {
    val zoomSpeed: Double
        get() = _zoomSpeed()

    val transition: TransitionType
        get() = _transition()
}
