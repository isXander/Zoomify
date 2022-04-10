package dev.isxander.zoomify.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.ZoomifySettings;
import dev.isxander.zoomify.utils.MathUtilsKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow private double eventDeltaWheel;

    @Inject(
            method = "onMouseScroll",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;eventDeltaWheel:D", ordinal = 7),
            cancellable = true
    )
    private void onMouseScroll(CallbackInfo ci) {
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

    @Redirect(
            method = "updateMouse",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;mouseSensitivity:D")
    )
    private double modifySensitivity(GameOptions options) {
        return options.mouseSensitivity / (ZoomifySettings.INSTANCE.getRelativeSensitivity() ? Zoomify.INSTANCE.getPreviousZoomDivisor() : 1);
    }
}
