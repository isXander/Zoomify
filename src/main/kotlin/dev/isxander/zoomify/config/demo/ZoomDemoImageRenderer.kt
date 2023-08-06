package dev.isxander.zoomify.config.demo

import dev.isxander.yacl3.gui.ImageRenderer
import dev.isxander.yacl3.gui.ImageRenderer.AnimatedNativeImageBacked
import dev.isxander.zoomify.utils.popPose
import dev.isxander.zoomify.utils.pushPose
import dev.isxander.zoomify.utils.scale
import dev.isxander.zoomify.utils.translate
import dev.isxander.zoomify.zoom.ZoomHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import java.util.Optional
import java.util.concurrent.CompletableFuture

abstract class ZoomDemoImageRenderer(val zoomHelper: ZoomHelper, private val zoomControl: ControlEmulation) : ImageRenderer {
    var keyDown = false
    var scrollTiers = 0

    init {
        zoomControl.setup(this)
    }

    abstract override fun render(graphics: GuiGraphics, x: Int, y: Int, renderWidth: Int): Int

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
        protected fun makeWebp(id: ResourceLocation): CompletableFuture<Optional<ImageRenderer>> {
            return ImageRenderer.getOrMakeAsync(id) { Optional.of(AnimatedNativeImageBacked.createWEBPFromTexture(id)) }
        }
    }

}

class FirstPersonDemo(zoomHelper: ZoomHelper, zoomControl: ControlEmulation) : ZoomDemoImageRenderer(zoomHelper, zoomControl) {
    var keepHandFov = false

    private val handRenderer = makeWebp(HAND_TEXTURE)
    private val worldRenderer = makeWebp(WORLD_TEXTURE)

    override fun render(graphics: GuiGraphics, x: Int, y: Int, renderWidth: Int): Int {
        val tickDelta = Minecraft.getInstance().frameTime

        val ratio = renderWidth / TEX_WIDTH.toDouble()
        val renderHeight = (TEX_HEIGHT * ratio).toInt()
        if (!handRenderer.isDone || !worldRenderer.isDone) {
            return renderHeight
        }

        graphics.enableScissor(x, y, x + renderWidth, y + renderHeight)

        graphics.pushPose()
        graphics.translate(x.toDouble(), y.toDouble(), 0.0)
        graphics.scale(ratio.toFloat(), ratio.toFloat(), 1f)

        val zoomScale = zoomHelper.getZoomDivisor(tickDelta).toFloat()
        graphics.pushPose()
        graphics.translate(TEX_WIDTH / 2f, TEX_HEIGHT / 2f, 0.0F)
        graphics.scale(zoomScale, zoomScale, 1.0F)
        graphics.translate(-TEX_WIDTH / 2f, -TEX_HEIGHT / 2f, 0.0F)

        worldRenderer.get().get().render(graphics, 0, 0, TEX_WIDTH)

        if (keepHandFov) graphics.popPose()
        handRenderer.get().get().render(graphics, 0, 0, TEX_WIDTH)
        if (!keepHandFov) graphics.popPose()

        graphics.popPose()

        graphics.disableScissor()

        return renderHeight
    }

    override fun close() {
        worldRenderer.getNow(Optional.empty()).map { it.close() }
        handRenderer.getNow(Optional.empty()).map { it.close() }
    }

    companion object {
        const val TEX_WIDTH = 1916
        const val TEX_HEIGHT = 910

        val WORLD_TEXTURE = ResourceLocation("zoomify", "textures/demo/zoom-world.webp")
        val HAND_TEXTURE = ResourceLocation("zoomify", "textures/demo/zoom-hand.webp")

    }
}

class ThirdPersonDemo(zoomHelper: ZoomHelper, zoomControl: ControlEmulation) : ZoomDemoImageRenderer(zoomHelper, zoomControl) {
    private val thirdPersonViewRenderer = makeWebp(PLAYER_VIEW)
    private val hudRenderer = makeWebp(HUD_TEXTURE)

    var renderHud = true

    override fun render(graphics: GuiGraphics, x: Int, y: Int, renderWidth: Int): Int {
        val tickDelta = Minecraft.getInstance().frameTime

        val ratio = renderWidth / TEX_WIDTH.toDouble()
        val renderHeight = (TEX_HEIGHT * ratio).toInt()
        if (!thirdPersonViewRenderer.isDone || !hudRenderer.isDone) {
            return renderHeight
        }

        graphics.enableScissor(x, y, x + renderWidth, y + renderHeight)

        graphics.pushPose()
        graphics.translate(x.toDouble(), y.toDouble(), 0.0)
        graphics.scale(ratio.toFloat(), ratio.toFloat(), 1f)

        val zoomScale = zoomHelper.getZoomDivisor(tickDelta).toFloat()
        graphics.pushPose()
        graphics.translate(FirstPersonDemo.TEX_WIDTH / 2f, FirstPersonDemo.TEX_HEIGHT / 2f, 0.0F)
        graphics.scale(zoomScale, zoomScale, 1.0F)
        graphics.translate(-FirstPersonDemo.TEX_WIDTH / 2f, -FirstPersonDemo.TEX_HEIGHT / 2f, 0.0F)

        thirdPersonViewRenderer.get().get().render(graphics, 0, 0, TEX_WIDTH)

        graphics.popPose()

        if (renderHud)
            hudRenderer.get().get().render(graphics, 0, 0, TEX_WIDTH)

        graphics.popPose()
        graphics.disableScissor()

        return renderHeight
    }

    override fun close() {
        thirdPersonViewRenderer.getNow(Optional.empty()).map { it.close() }
        hudRenderer.getNow(Optional.empty()).map { it.close() }
    }

    companion object {
        const val TEX_WIDTH = 1915
        const val TEX_HEIGHT = 910

        val PLAYER_VIEW = ResourceLocation("zoomify", "textures/demo/third-person-view.webp")
        val HUD_TEXTURE = ResourceLocation("zoomify", "textures/demo/third-person-hud.webp")
    }
}
