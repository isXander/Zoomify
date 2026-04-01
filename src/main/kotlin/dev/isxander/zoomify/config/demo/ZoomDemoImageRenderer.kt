package dev.isxander.zoomify.config.demo

import dev.isxander.yacl3.gui.image.ImageRenderer
import dev.isxander.yacl3.gui.image.ImageRendererManager
import dev.isxander.yacl3.gui.image.impl.AnimatedDynamicTextureImage
import dev.isxander.zoomify.utils.*
import dev.isxander.zoomify.zoom.ZoomHelper
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier
import java.util.Optional
import java.util.concurrent.CompletableFuture

abstract class ZoomDemoImageRenderer(val zoomHelper: ZoomHelper, private val zoomControl: ControlEmulation) : ImageRenderer {
    var keyDown = false
    var scrollTiers = 0

    init {
        zoomControl.setup(this)
    }

    abstract override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, renderWidth: Int, deltaTime: Float): Int

    override fun tick() {
        zoomHelper.tick(keyDown, scrollTiers)
        zoomControl.tick(this)
    }

    fun pause() {
        zoomHelper.setToZero()
        zoomControl.pause(this)
    }

    companion object {
        @JvmStatic
        protected fun makeWebp(id: Identifier): CompletableFuture<AnimatedDynamicTextureImage> {
            return ImageRendererManager.registerOrGetImage(id) { AnimatedDynamicTextureImage.createWEBPFromTexture(id) }
        }
    }

}

class FirstPersonDemo(zoomHelper: ZoomHelper, zoomControl: ControlEmulation) : ZoomDemoImageRenderer(zoomHelper, zoomControl) {
    var keepHandFov = false

    private val handRenderer = makeWebp(HAND_TEXTURE)
    private val worldRenderer = makeWebp(WORLD_TEXTURE)

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, renderWidth: Int, deltaTime: Float): Int {
        val ratio = renderWidth / TEX_WIDTH.toDouble()
        val renderHeight = (TEX_HEIGHT * ratio).toInt()
        if (!handRenderer.isDone || !worldRenderer.isDone) {
            return renderHeight
        }

        graphics.enableScissor(x, y, x + renderWidth, y + renderHeight)

        graphics.pose().pushMatrix()
        graphics.pose().translate(x.toFloat(), y.toFloat())
        graphics.pose().scale(ratio.toFloat(), ratio.toFloat())

        val zoomScale = zoomHelper.getZoomDivisor(deltaTime).toFloat()
        graphics.pose().pushMatrix()
        graphics.pose().translate(TEX_WIDTH / 2f, TEX_HEIGHT / 2f)
        graphics.pose().scale(zoomScale, zoomScale)
        graphics.pose().translate(-TEX_WIDTH / 2f, -TEX_HEIGHT / 2f)

        worldRenderer.get().render(graphics, 0, 0, TEX_WIDTH, deltaTime)

        if (keepHandFov) graphics.pose().popMatrix()
        handRenderer.get().render(graphics, 0, 0, TEX_WIDTH, deltaTime)
        if (!keepHandFov) graphics.pose().popMatrix()

        graphics.pose().popMatrix()

        graphics.disableScissor()

        return renderHeight
    }

    override fun close() {
        Optional.ofNullable(worldRenderer.getNow(null)).map { it.close() }
        Optional.ofNullable(handRenderer.getNow(null)).map { it.close() }
    }

    companion object {
        const val TEX_WIDTH = 1916
        const val TEX_HEIGHT = 910

        val WORLD_TEXTURE = zoomifyRl("textures/demo/zoom-world.webp")
        val HAND_TEXTURE = zoomifyRl("textures/demo/zoom-hand.webp")

    }
}

class ThirdPersonDemo(zoomHelper: ZoomHelper, zoomControl: ControlEmulation) : ZoomDemoImageRenderer(zoomHelper, zoomControl) {
    private val thirdPersonViewRenderer = makeWebp(PLAYER_VIEW)
    private val hudRenderer = makeWebp(HUD_TEXTURE)

    var renderHud = true

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, renderWidth: Int, deltaTime: Float): Int {
        val ratio = renderWidth / TEX_WIDTH.toDouble()
        val renderHeight = (TEX_HEIGHT * ratio).toInt()
        if (!thirdPersonViewRenderer.isDone || !hudRenderer.isDone) {
            return renderHeight
        }

        graphics.enableScissor(x, y, x + renderWidth, y + renderHeight)

        graphics.pose().pushMatrix()
        graphics.pose().translate(x.toFloat(), y.toFloat())
        graphics.pose().scale(ratio.toFloat(), ratio.toFloat())

        val zoomScale = zoomHelper.getZoomDivisor(deltaTime).toFloat()
        graphics.pose().pushMatrix()
        graphics.pose().translate(FirstPersonDemo.TEX_WIDTH / 2f, FirstPersonDemo.TEX_HEIGHT / 2f)
        graphics.pose().scale(zoomScale, zoomScale)
        graphics.pose().translate(-FirstPersonDemo.TEX_WIDTH / 2f, -FirstPersonDemo.TEX_HEIGHT / 2f)

        thirdPersonViewRenderer.get().render(graphics, 0, 0, TEX_WIDTH, deltaTime)

        graphics.pose().popMatrix()

        if (renderHud)
            hudRenderer.get().render(graphics, 0, 0, TEX_WIDTH, deltaTime)

        graphics.pose().popMatrix()
        graphics.disableScissor()

        return renderHeight
    }

    override fun close() {
        Optional.ofNullable(thirdPersonViewRenderer.getNow(null)).map { it.close() }
        Optional.ofNullable(hudRenderer.getNow(null)).map { it.close() }
    }

    companion object {
        const val TEX_WIDTH = 1915
        const val TEX_HEIGHT = 910

        val PLAYER_VIEW = zoomifyRl("textures/demo/third-person-view.webp")
        val HUD_TEXTURE = zoomifyRl("textures/demo/third-person-hud.webp")
    }
}
