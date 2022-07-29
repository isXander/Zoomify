package dev.isxander.zoomify.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.SpyglassBehaviour;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.Mouse;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow private double eventDeltaWheel;

    @Inject(
        method = "onMouseScroll",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;eventDeltaWheel:D", ordinal = 7),
        cancellable = true
    )
    private void scrollStepCounter(CallbackInfo ci) {
        if (ZoomifySettings.INSTANCE.getScrollZoom() && Zoomify.INSTANCE.getZooming() && eventDeltaWheel != 0) {
            Zoomify.mouseZoom(eventDeltaWheel);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
        method = "updateMouse",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;smoothCameraEnabled:Z")
    )
    private boolean smoothCameraIfZoom(boolean original) {
        return original || (Zoomify.INSTANCE.getZooming() && ZoomifySettings.INSTANCE.getCinematicCam());
    }

    @ModifyExpressionValue(
        method = "updateMouse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;",
            ordinal = 0
        )
    )
    private Object applyRelativeSensitivity(Object genericValue) {
        double value = (Double) genericValue;
        return value / MathHelper.lerp(ZoomifySettings.INSTANCE.getRelativeSensitivity() / 100.0, 1.0, Zoomify.INSTANCE.getPreviousZoomDivisor());
    }

    @ModifyExpressionValue(
        method = "updateMouse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingSpyglass()Z"
        )
    )
    private boolean shouldApplySpyglassSensitivity(boolean isUsingSpyglass) {
        if (ZoomifySettings.INSTANCE.getSpyglassBehaviour() != SpyglassBehaviour.COMBINE)
            return false;
        return isUsingSpyglass;
    }
}
