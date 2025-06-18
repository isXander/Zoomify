package dev.isxander.zoomify.mixins.zoom.secondary;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Gui.class)
public class GuiMixin {
    //? if >=1.21.6 {
    @WrapMethod(method = "render")
    private void preventHudRender(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker, Operation<Void> original) {
        if (!Zoomify.INSTANCE.getSecondaryZooming() || !ZoomifySettings.Companion.getSecondaryHideHUDOnZoom().get()) {
            original.call(guiGraphics, deltaTracker);
        }
    }
    //?}
}
