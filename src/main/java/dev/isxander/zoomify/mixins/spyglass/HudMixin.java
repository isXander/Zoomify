package dev.isxander.zoomify.mixins.spyglass;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.zoomify.Zoomify;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.2 {
@Mixin(net.minecraft.client.gui.Hud.class)
//?} else {
/*@Mixin(net.minecraft.client.gui.Gui.class)
*///?}
public class HudMixin {
    @Shadow @Final private Minecraft minecraft;

    @ModifyExpressionValue(
        method = "extractCameraOverlays",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"
        )
    )
    private boolean shouldRenderSpyglassOverlay(boolean isUsingSpyglass) {
        return Zoomify.shouldRenderOverlay(minecraft.player, isUsingSpyglass);
    }
}
