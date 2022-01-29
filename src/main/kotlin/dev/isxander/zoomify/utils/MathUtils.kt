package dev.isxander.zoomify.utils

fun getPercent(num: Double, min: Double = 0.0, max: Double = 100.0): Double {
    return (num - min) / (max - min)
}

fun lerp(a: Double, b: Double, t: Double): Double {
    return a + (b - a) * t
}
