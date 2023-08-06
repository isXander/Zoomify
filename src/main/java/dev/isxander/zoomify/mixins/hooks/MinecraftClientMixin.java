package dev.isxander.zoomify.mixins.hooks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.zoomify.Zoomify;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @ModifyExpressionValue(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;"
        )
    )
    private ReloadInstance onReloadResources(ReloadInstance resourceReload) {
        resourceReload.done().thenRun(Zoomify.INSTANCE::onGameFinishedLoading);
        return resourceReload;
    }
}
