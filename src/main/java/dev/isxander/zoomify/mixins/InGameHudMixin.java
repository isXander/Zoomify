package dev.isxander.zoomify.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.SpyglassBehaviour;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @ModifyExpressionValue(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingSpyglass()Z"
        )
    )
    private boolean shouldRenderSpyglassOverlay(boolean isUsingSpyglass) {
        switch (ZoomifySettings.INSTANCE.getSpyglassOverlayVisibility()) {
            case DEFAULT -> {
                return isUsingSpyglass;
            }
            case ALWAYS -> {
                return Zoomify.INSTANCE.getZooming();
            }
            case HOLDING -> {
                return isUsingSpyglass || (Zoomify.INSTANCE.getZooming() && client.player.isHolding(Items.SPYGLASS) && ZoomifySettings.INSTANCE.getSpyglassBehaviour() != SpyglassBehaviour.COMBINE);
            }
            case CARRYING -> {
                return Zoomify.INSTANCE.getZooming() && client.player.getInventory().containsAny(stack -> stack.isOf(Items.SPYGLASS));
            }
        }
        return isUsingSpyglass;
    }
}
