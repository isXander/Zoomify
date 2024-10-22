package dev.isxander.zoomify.mixins.spyglass;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.zoomify.Zoomify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class InGameHudMixin {
    @Shadow @Final private Minecraft minecraft;

    @ModifyExpressionValue(
        /*? if >1.20.4 {*/
        method = "renderCameraOverlays",
        /*?} else {*/
        /*method = "render",
        *//*?}*/
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"
        )
    )
    private boolean shouldRenderSpyglassOverlay(boolean isUsingSpyglass) {
        return Zoomify.shouldRenderOverlay(minecraft.player, isUsingSpyglass);
    }
}
