/*
 * Copyright (C) 2026 isXander
 * This file is part of Zoomify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.zoomify.neoforge

import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.config.createSettingsGui
import dev.isxander.zoomify.platform.ZoomifyPlatform
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@Mod("zoomify")
object ZoomifyBootstrap {
	init {
		ZoomifyPlatform.instance = NeoforgeZoomifyPlatform

		ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory::class.java) {
			IConfigScreenFactory { _, parent -> createSettingsGui(parent) }
		}

		Zoomify.onInitializeClient(NeoforgeZoomifyPlatform)
	}
}
