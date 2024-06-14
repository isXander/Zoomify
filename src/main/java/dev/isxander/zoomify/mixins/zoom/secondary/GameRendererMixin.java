package dev.isxander.zoomify.mixins.zoom.secondary;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.renderer.GameRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyExpressionValue(
        method = "render",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Options;hideGui:Z",
            opcode = Opcodes.GETFIELD
        )
    )
    private boolean shouldHideHUD(boolean hideHUD) {
        return hideHUD || (Zoomify.INSTANCE.getSecondaryZooming() && ZoomifySettings.INSTANCE.getSecondaryHideHUDOnZoom().get());
    }
}
