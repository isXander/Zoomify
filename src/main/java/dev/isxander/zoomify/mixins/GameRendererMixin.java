package dev.isxander.zoomify.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.SpyglassBehaviour;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.text.MessageFormat;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyReturnValue(
        method = "getFov",
        at = @At("RETURN")
    )
    private double modifyFovWithZoom(double fov) {
        return fov * Zoomify.getZoomDivisor();
    }

    @ModifyExpressionValue(
        method = "bobView",
        at = {
            @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;prevStrideDistance:F", opcode = Opcodes.GETFIELD),
            @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;strideDistance:F", opcode = Opcodes.GETFIELD),
        }
    )
    private float modifyBobbingIntensity(float p) {
        if (!ZoomifySettings.INSTANCE.getRelativeViewBobbing())
            return p;

        return (float) (p / MathHelper.lerp(0.2, 1.0, 1 / Zoomify.INSTANCE.getPreviousZoomDivisor()));
    }
}
