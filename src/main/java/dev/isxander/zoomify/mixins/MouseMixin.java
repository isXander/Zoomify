package dev.isxander.zoomify.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow private double eventDeltaWheel;

    @Inject(
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;eventDeltaWheel:D", ordinal = 7),
            method = "onMouseScroll",
            cancellable = true
    )
    private void onMouseScroll(CallbackInfo ci) {
        if (Zoomify.INSTANCE.getZoomKey().isPressed() && eventDeltaWheel != 0) {
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
}
