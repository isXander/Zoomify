/*
 * Copyright (C) 2026 isXander
 * This file is part of Zoomify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.zoomify.fabric.integrations

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.zoomify.config.createSettingsGui

object ModMenuIntegration : ModMenuApi {
	override fun getModConfigScreenFactory(): ConfigScreenFactory<*> = ConfigScreenFactory { parent ->
		createSettingsGui(parent)
	}
}
