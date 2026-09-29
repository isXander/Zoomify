/*
 * Copyright (C) 2026 isXander
 * This file is part of Zoomify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.zoomify.utils

import dev.isxander.yacl3.api.NameableEnum
import net.minecraft.network.chat.Component

interface NameableEnumKt : NameableEnum {
	val localisedName: Component

	override fun getDisplayName(): Component = localisedName
}
