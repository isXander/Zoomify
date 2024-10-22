package dev.isxander.zoomify.mixins.zoom;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyReturnValue(
        method = "getFov",
        at = @At("RETURN")
    )
    private /*$ fov-precision >>*/ float modifyFovWithZoom(
            /*$ fov-precision >>*/ float fov,
            @Local(argsOnly = true) float tickDelta
    ) {
        return fov / Zoomify.getZoomDivisor(tickDelta);
    }

    @ModifyExpressionValue(
        method = "bobView",
        at = {
            @At(
                    value = "FIELD",
                    //? if >=1.21.2 {
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;oBob:F",
                    //?} else {
                    /*target = "Lnet/minecraft/world/entity/player/Player;oBob:F",
                    *///?}
                    opcode = Opcodes.GETFIELD
            ),
            @At(
                    value = "FIELD",
                    //? if >=1.21.2 {
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;bob:F",
                    //?} else {
                    /*target = "Lnet/minecraft/world/entity/player/Player;bob:F",
                    *///?}
                    opcode = Opcodes.GETFIELD
            ),
        }
    )
    private float modifyBobbingIntensity(float p) {
        if (!ZoomifySettings.Companion.getRelativeViewBobbing().get())
            return p;

        return (float) (p / Mth.lerp(0.2, 1.0, Zoomify.INSTANCE.getPreviousZoomDivisor()));
    }

    @ModifyExpressionValue(
        method = "renderItemInHand",
        at = @At(
            value = "INVOKE",
            //? if >=1.21.2 {
            target = "Lnet/minecraft/client/renderer/GameRenderer;getFov(Lnet/minecraft/client/Camera;FZ)F"
            //?} else {
            /*target = "Lnet/minecraft/client/renderer/GameRenderer;getFov(Lnet/minecraft/client/Camera;FZ)D"
            *///?}
        )
    )
    private /*$ fov-precision >>*/ float keepHandFov(
            /*$ fov-precision >>*/ float fov,
            @Local(argsOnly=true) float tickDelta
    ) {
        if (!ZoomifySettings.Companion.getAffectHandFov().get())
            return fov * Zoomify.getZoomDivisor(tickDelta);
        return fov;
    }
}
