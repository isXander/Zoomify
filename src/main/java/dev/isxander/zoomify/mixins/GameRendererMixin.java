package dev.isxander.zoomify.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyReturnValue(
        method = "getFov",
        at = @At("RETURN")
    )
    private double modifyFovWithZoom(double fov, Camera camera, float tickDelta, boolean changingFov) {
        return fov / Zoomify.getZoomDivisor(tickDelta);
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

        return (float) (p / MathHelper.lerp(0.2, 1.0, Zoomify.INSTANCE.getPreviousZoomDivisor()));
    }

    @ModifyExpressionValue(
        method = "renderHand",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/GameRenderer;getFov(Lnet/minecraft/client/render/Camera;FZ)D"
        )
    )
    private double keepHandFov(double fov, MatrixStack matrices, Camera camera, float tickDelta) {
        if (!ZoomifySettings.INSTANCE.getAffectHandFov())
            return fov * Zoomify.getZoomDivisor(tickDelta);
        return fov;
    }
}
