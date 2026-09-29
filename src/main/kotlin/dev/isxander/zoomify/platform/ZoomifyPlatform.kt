/*
 * Copyright (C) 2026 isXander
 * This file is part of Zoomify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.zoomify.platform

import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import java.nio.file.Path

interface ZoomifyPlatform {
	val configDir: Path

	fun registerKeyMapping(keyMapping: KeyMapping)

	fun onClientTickEnd(block: (Minecraft) -> Unit)

	fun registerSimpleClientCommand(literal: String, executor: () -> Unit)

	companion object {
		lateinit var instance: ZoomifyPlatform
	}
}
