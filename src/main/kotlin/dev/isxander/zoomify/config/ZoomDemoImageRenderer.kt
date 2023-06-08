package dev.isxander.zoomify.config

import dev.isxander.yacl3.gui.ImageRenderer
import dev.isxander.zoomify.zoom.ZoomHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier

class ZoomDemoImageRenderer(val zoomHelper: ZoomHelper) : ImageRenderer {
    var time = 0f
    var keyDown = true
    var switchPauseTicks = 0
    var keepHandFov = false

    override fun render(graphics: DrawContext, x: Int, y: Int, renderWidth: Int): Int {
        val tickDelta = MinecraftClient.getInstance().tickDelta

        val ratio = renderWidth / TEX_WIDTH.toDouble()
        val renderHeight = (TEX_HEIGHT * ratio).toInt()

        graphics.enableScissor(x, y, x + renderWidth, y + renderHeight)

        graphics.matrices.push()
        graphics.matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        graphics.matrices.scale(ratio.toFloat(), ratio.toFloat(), 1f)

        val zoomScale = zoomHelper.getZoomDivisor(tickDelta).toFloat()
        graphics.matrices.push()
        graphics.matrices.translate(TEX_WIDTH / 2f, TEX_HEIGHT / 2f, 0.0F)
        graphics.matrices.scale(zoomScale, zoomScale, 1.0F)
        graphics.matrices.translate(-TEX_WIDTH / 2f, -TEX_HEIGHT / 2f, 0.0F)

        graphics.drawTexture(WORLD_TEXTURE, 0, 0, 0f, 0f, TEX_WIDTH, TEX_HEIGHT, TEX_WIDTH, TEX_HEIGHT)

        if (keepHandFov) graphics.matrices.pop()
        graphics.drawTexture(HAND_TEXTURE, 0, 0, 0f, 0f, TEX_WIDTH, TEX_HEIGHT, TEX_WIDTH, TEX_HEIGHT)
        if (!keepHandFov) graphics.matrices.pop()

        graphics.matrices.pop()

        graphics.disableScissor()

        time += tickDelta
        while (time > 1f) {
            if (switchPauseTicks > 0) {
                switchPauseTicks -= 1
                if (switchPauseTicks == 0)
                    keyDown = !keyDown
            } else {
                if (zoomScale == zoomHelper.getZoomDivisor(0f).toFloat())
                    switchPauseTicks = 20
            }

            zoomHelper.tick(keyDown, 0)
            time -= 1f
        }

        return renderHeight
    }

    override fun close() {

    }

    companion object {
        const val TEX_WIDTH = 1916
        const val TEX_HEIGHT = 910

        val WORLD_TEXTURE = Identifier("zoomify", "textures/demo/zoom-world.png")
        val HAND_TEXTURE = Identifier("zoomify", "textures/demo/zoom-hand.png")
    }
}
