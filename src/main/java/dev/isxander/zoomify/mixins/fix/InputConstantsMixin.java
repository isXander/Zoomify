package dev.isxander.zoomify.mixins.fix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.ByteBuffer;

//? if >=26.3 {
/**
 * Vanilla 26.3 reads the SDL keyboard state with no bounds check
 * ({@code SDL_GetKeyboardState().get(key)}), so a single keybinding with an
 * out-of-range code (registered by any mod, or left over in options.txt)
 * crashes the game on every input event. Treat such keys as not pressed.
 */
@Mixin(InputConstants.class)
public class InputConstantsMixin {
    @WrapOperation(
            method = "isKeyDown",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/ByteBuffer;get(I)B"
            )
    )
    private static byte guardKeyboardStateIndex(ByteBuffer buffer, int index, Operation<Byte> original) {
        if (index < 0 || index >= buffer.limit()) {
            return 0;
        }
        return original.call(buffer, index);
    }
}
//?} else {
/*@Mixin(InputConstants.class)
public class InputConstantsMixin {
}*///?}
