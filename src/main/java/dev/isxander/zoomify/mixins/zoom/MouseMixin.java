package dev.isxander.zoomify.mixins.zoom;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.SpyglassBehaviour;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {

    /*? if <=1.20.1 { *//*
    @Shadow
    private double accumulatedScroll;
    *//*? } */

    @Inject(
        method = "onScroll",
        /*? if >1.20.1 {*/
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"),
        /*? } else { *//*
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;accumulatedScroll:D", ordinal = 7),
        *//*? } */
        cancellable = true
    )
    private void scrollStepCounter(CallbackInfo ci /*? if >1.20.1 { */, @Local(ordinal = 1) int scrollY /*? } */) {
        /*? if <=1.20.1 { *//*
        double scrollY = accumulatedScroll;
        *//*? } */

        if (ZoomifySettings.INSTANCE.getScrollZoom().get()
                && Zoomify.INSTANCE.getZooming() && scrollY != 0
                && !ZoomifySettings.INSTANCE.getKeybindScrolling()) {
            Zoomify.mouseZoom(scrollY);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
        method = "turnPlayer",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;smoothCamera:Z")
    )
    private boolean smoothCameraIfZoom(boolean original) {
        return original
                || Zoomify.INSTANCE.getSecondaryZooming()
                || (Zoomify.INSTANCE.getZooming() && ZoomifySettings.INSTANCE.getCinematicCamera().get() > 0);
    }

    @ModifyExpressionValue(
        method = "turnPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;",
            ordinal = 0
        )
    )
    private Object applyRelativeSensitivity(Object genericValue) {
        double value = (Double) genericValue;
        return value / Mth.lerp(
                ZoomifySettings.INSTANCE.getRelativeSensitivity().get() / 100.0,
                1.0,
                Zoomify.INSTANCE.getPreviousZoomDivisor()
        );
    }

    @ModifyExpressionValue(
        method = "turnPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"
        )
    )
    private boolean shouldApplySpyglassSensitivity(boolean isUsingSpyglass) {
        if (ZoomifySettings.INSTANCE.getSpyglassBehaviour().get() != SpyglassBehaviour.COMBINE)
            return false;
        return isUsingSpyglass;
    }

    @ModifyArg(
        method = "turnPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/SmoothDouble;getNewDeltaValue(DD)D"
        ),
        index = 1
    )
    private double modifyCinematicSmoothness(double smoother) {
        if (Zoomify.INSTANCE.getZooming() && ZoomifySettings.INSTANCE.getCinematicCamera().get() > 0)
            return smoother / (ZoomifySettings.INSTANCE.getCinematicCamera().get() / 100.0);

        return smoother;
    }

}
