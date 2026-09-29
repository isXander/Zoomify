/*
 * Copyright (C) 2026 isXander
 * This file is part of Zoomify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.zoomify.neoforge

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.isxander.zoomify.platform.ZoomifyPlatform
import dev.isxander.zoomify.utils.minecraft
import dev.nyon.klf.MOD_BUS
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.common.NeoForge
import java.nio.file.Path

object NeoforgeZoomifyPlatform : ZoomifyPlatform {
	override val configDir: Path
		get() = FMLPaths.CONFIGDIR.get()

	override fun registerKeyMapping(keyMapping: KeyMapping) {
		MOD_BUS.addListener<RegisterKeyMappingsEvent> { event ->
			event.register(keyMapping)
		}
	}

	override fun onClientTickEnd(block: (Minecraft) -> Unit) {
		NeoForge.EVENT_BUS.addListener<ClientTickEvent.Pre> {
			block(minecraft)
		}
	}

	override fun registerSimpleClientCommand(literal: String, executor: () -> Unit) {
		NeoForge.EVENT_BUS.addListener<RegisterClientCommandsEvent> { event ->
			event.dispatcher.register(LiteralArgumentBuilder.literal<CommandSourceStack>(literal).executes {
				executor()
				0
			})
		}
	}
}
