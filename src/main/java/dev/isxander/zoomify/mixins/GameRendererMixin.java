package dev.isxander.zoomify.mixins;

import dev.isxander.zoomify.Zoomify;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyVariable(method = "getFov", at = @At(value = "RETURN", shift = At.Shift.BEFORE), ordinal = 0)
    private double getFov(double fov) {
        return fov / Zoomify.getZoomDivisor(MinecraftClient.getInstance().getTickDelta());
    }
}
