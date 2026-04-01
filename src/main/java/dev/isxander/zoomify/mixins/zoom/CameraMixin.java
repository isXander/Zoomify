package dev.isxander.zoomify.mixins.zoom;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {
    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float modifyFovWithZoom(float fov, @Local(name = "partialTicks", argsOnly = true) float partialTicks) {
        return fov / Zoomify.getZoomDivisor(partialTicks);
    }

    @ModifyReturnValue(method = "calculateHudFov", at = @At("RETURN"))
    private float modifyHudFovWithZoom(float fov, @Local(name = "partialTicks", argsOnly = true) float partialTicks) {
        if (ZoomifySettings.Companion.getAffectHandFov().get())
            return fov / Zoomify.getZoomDivisor(partialTicks);
        return fov;
    }

    @ModifyExpressionValue(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/ClientAvatarState;getInterpolatedBob(F)F"
            )
    )
    private float modifyBobbingIntensity(float interpolatedBob) {
        if (!ZoomifySettings.Companion.getRelativeViewBobbing().get())
            return interpolatedBob;

        return (float) (interpolatedBob / Mth.lerp(0.2, 1.0, Zoomify.INSTANCE.getPreviousZoomDivisor()));
    }
}
