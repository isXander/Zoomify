package dev.isxander.zoomify.mixins;

import dev.isxander.zoomify.config.SpyglassBehaviour;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
    @ModifyConstant(
        method = "getFovMultiplier",
        constant = @Constant(floatValue = 0.1f)
    )
    private float modifySpyglassFovMultiplier(float multiplier) {
        if (ZoomifySettings.INSTANCE.getSpyglassBehaviour() != SpyglassBehaviour.COMBINE)
            return 1f;
        return multiplier;
    }
}
