/*
 * Copyright (C) 2026 isXander
 * This file is part of Zoomify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.zoomify.fabric

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.isxander.zoomify.platform.ZoomifyPlatform
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import java.nio.file.Path

object FabricZoomifyPlatform : ZoomifyPlatform {
	override val configDir: Path
		get() = FabricLoader.getInstance().configDir

	override fun registerKeyMapping(keyMapping: KeyMapping) {
		KeyMappingHelper.registerKeyMapping(keyMapping)
	}

	override fun onClientTickEnd(block: (Minecraft) -> Unit) {
		ClientTickEvents.END_CLIENT_TICK.register(block::invoke)
	}

	override fun registerSimpleClientCommand(literal: String, executor: () -> Unit) {
		ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			dispatcher.register(LiteralArgumentBuilder.literal<FabricClientCommandSource>(literal).executes {
				executor()
				0
			})
		}
	}
}
