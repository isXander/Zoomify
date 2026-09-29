/*
 * Copyright (C) 2026 isXander
 * This file is part of Zoomify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.zoomify.mixins.spyglass;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.zoomify.config.SpyglassBehaviour;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerEntityMixin {
	@ModifyExpressionValue(
		method = "getFieldOfViewModifier",
		at = @At(value = "CONSTANT", args = "floatValue=0.1f")
	)
	private float modifySpyglassFovMultiplier(float multiplier) {
		if (ZoomifySettings.Companion.getSpyglassBehaviour().get() != SpyglassBehaviour.COMBINE)
			return 1f;
		return multiplier;
	}
}
