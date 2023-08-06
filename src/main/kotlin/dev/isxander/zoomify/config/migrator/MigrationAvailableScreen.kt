package dev.isxander.zoomify.config.migrator

import dev.isxander.zoomify.config.ZoomifySettings
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConfirmScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class MigrationAvailableScreen(migrator: Migrator, parent: Screen?) : ConfirmScreen(
    { yes ->
        if (yes) {
            val migration = Migration()
            migrator.migrate(migration)
            ZoomifySettings.export()
            Minecraft.getInstance().setScreen(MigrationResultScreen(migration, parent))
        } else {
            Minecraft.getInstance().setScreen(parent)
        }
    },
    Component.translatable("zoomify.migrate.available.title", migrator.name.copy().withStyle(ChatFormatting.BOLD)),
    Component.translatable("zoomify.migrate.available.message", migrator.name)
)
