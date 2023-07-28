package dev.isxander.zoomify.config

import dev.isxander.yacl3.gui.ImageRenderer
import dev.isxander.yacl3.gui.ImageRenderer.AnimatedNativeImageBacked
import dev.isxander.zoomify.zoom.ZoomHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import java.util.Optional
import java.util.concurrent.CompletableFuture

class ZoomDemoImageRenderer(private val zoomHelper: ZoomHelper) : ImageRenderer {
    private var keyDown = true
    private var switchPauseTicks = 0
    var keepHandFov = false

    private val handRenderer = makeWebp(HAND_TEXTURE)
    private val worldRenderer = makeWebp(WORLD_TEXTURE)

    override fun render(graphics: DrawContext, x: Int, y: Int, renderWidth: Int): Int {
        val tickDelta = MinecraftClient.getInstance().tickDelta

        val ratio = renderWidth / TEX_WIDTH.toDouble()
        val renderHeight = (TEX_HEIGHT * ratio).toInt()
        if (!handRenderer.isDone || !worldRenderer.isDone) {
            return renderHeight
        }

        graphics.enableScissor(x, y, x + renderWidth, y + renderHeight)

        graphics.matrices.push()
        graphics.matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        graphics.matrices.scale(ratio.toFloat(), ratio.toFloat(), 1f)

        val zoomScale = zoomHelper.getZoomDivisor(tickDelta).toFloat()
        graphics.matrices.push()
        graphics.matrices.translate(TEX_WIDTH / 2f, TEX_HEIGHT / 2f, 0.0F)
        graphics.matrices.scale(zoomScale, zoomScale, 1.0F)
        graphics.matrices.translate(-TEX_WIDTH / 2f, -TEX_HEIGHT / 2f, 0.0F)

        worldRenderer.get().get().render(graphics, 0, 0, TEX_WIDTH)

        if (keepHandFov) graphics.matrices.pop()
        handRenderer.get().get().render(graphics, 0, 0, TEX_WIDTH)
        if (!keepHandFov) graphics.matrices.pop()

        graphics.matrices.pop()

        graphics.disableScissor()

        return renderHeight
    }

    override fun tick() {
        zoomHelper.tick(keyDown, 0)

        // if the previous state is identical to the current state, pause for a bit, then inverse what we just did
        if (switchPauseTicks > 0) {
            switchPauseTicks--
            if (switchPauseTicks == 0) {
                keyDown = !keyDown
            }
        } else if (zoomHelper.getZoomDivisor(1f) == zoomHelper.getZoomDivisor(0f)) {
            switchPauseTicks = 20
        }
    }

    override fun close() {
        worldRenderer.getNow(Optional.empty()).map { it.close() }
        handRenderer.getNow(Optional.empty()).map { it.close() }
    }

    fun pause() {
        zoomHelper.setToZero()
        keyDown = false
        switchPauseTicks = 20
    }

    companion object {
        const val TEX_WIDTH = 1916
        const val TEX_HEIGHT = 910

        val WORLD_TEXTURE = Identifier("zoomify", "textures/demo/zoom-world.webp")
        val HAND_TEXTURE = Identifier("zoomify", "textures/demo/zoom-hand.webp")

        fun makeWebp(id: Identifier): CompletableFuture<Optional<ImageRenderer>> {
            return ImageRenderer.getOrMakeAsync(id) { Optional.of(AnimatedNativeImageBacked.createWEBPFromTexture(id)) }
        }
    }
}
