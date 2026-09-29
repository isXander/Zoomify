/*
 * Copyright (C) 2026 isXander
 * This file is part of Zoomify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.zoomify.fabric

import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.platform.ZoomifyPlatform
import net.fabricmc.api.ClientModInitializer

object ZoomifyBootstrap : ClientModInitializer {
	override fun onInitializeClient() {
		ZoomifyPlatform.instance = FabricZoomifyPlatform
		Zoomify.onInitializeClient(FabricZoomifyPlatform)
	}
}
