package dev.isxander.zoomify.mixins.spyglass;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.isxander.zoomify.config.OverlayVisibility;
import dev.isxander.zoomify.config.SoundBehaviour;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpyglassItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpyglassItem.class)
public class SpyglassItemMixin {
    @WrapWithCondition(
        method = {"use", "stopUsing"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"
        )
    )
    private boolean shouldPlaySpyglassSound(Player instance, SoundEvent event, float volume, float pitch) {
        if (ZoomifySettings.Companion.getSpyglassSoundBehaviour().get() == SoundBehaviour.NEVER)
            return false;

        if (ZoomifySettings.Companion.getSpyglassSoundBehaviour().get() == SoundBehaviour.WITH_OVERLAY
                && ZoomifySettings.Companion.getSpyglassOverlayVisibility().get() == OverlayVisibility.NEVER) {
            return false;
        }

        return true;
    }
}
