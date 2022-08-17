package dev.isxander.zoomify.mixins.spyglass;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.isxander.zoomify.config.OverlayVisibility;
import dev.isxander.zoomify.config.SoundBehaviour;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SpyglassItem;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpyglassItem.class)
public class SpyglassItemMixin {
    @WrapWithCondition(
        method = {"use", "playStopUsingSound"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"
        )
    )
    private boolean shouldPlaySpyglassSound(PlayerEntity instance, SoundEvent event, float volume, float pitch) {
        if (ZoomifySettings.INSTANCE.getSpyglassSoundBehaviour() == SoundBehaviour.NEVER)
            return false;

        if (ZoomifySettings.INSTANCE.getSpyglassSoundBehaviour() == SoundBehaviour.WITH_OVERLAY && ZoomifySettings.INSTANCE.getSpyglassOverlayVisibility() == OverlayVisibility.NEVER)
            return false;

        return true;
    }
}
