package dev.isxander.zoomify.zoom

import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.config.ZoomifySettings
import net.minecraft.util.math.MathHelper

class TieredZoomHelper {
    private val zoomSpeed: Double
        get() = ZoomifySettings.scrollZoomSpeed / 100.0

    private val maxTiers: Int
        get() = Zoomify.maxScrollTiers

    val maxZoom: Double
        get() = ZoomifySettings.maxScrollZoom / 100.0 * 5

    private var interpolation = 0.0
    private var lastTier = 0
    private var resetting = false

    fun getZoomDivisor(tier: Int, tickDelta: Float): Double {
        if (tier > lastTier)
            resetting = false

        val targetZoom = tier.toDouble() / maxTiers

        interpolation = if (ZoomifySettings.smoothScrollZoom) {
            MathHelper.lerp(tickDelta * 0.25 * if (resetting) ZoomifySettings.zoomSpeed / 100.0 else zoomSpeed, interpolation, targetZoom)
        } else {
            targetZoom
        }

        return MathHelper.lerp(
            interpolation,
            0.0,
            maxZoom
        ).also { lastTier = tier }
    }

    fun resetTiers() {
        resetting = true
    }
}
