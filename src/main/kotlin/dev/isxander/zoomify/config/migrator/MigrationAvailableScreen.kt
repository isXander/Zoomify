package dev.isxander.zoomify.config.migrator

import dev.isxander.zoomify.config.ZoomifySettings
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class MigrationAvailableScreen(migrator: Migrator, parent: Screen?) : ConfirmScreen(
    { yes ->
        if (yes) {
            val migration = Migration()
            migrator.migrate(migration)
            ZoomifySettings.export()
            MinecraftClient.getInstance().setScreen(MigrationResultScreen(migration, parent))
        } else {
            MinecraftClient.getInstance().setScreen(parent)
        }
    },
    Text.translatable("zoomify.migrate.available.title", migrator.name.copy().formatted(Formatting.BOLD)),
    Text.translatable("zoomify.migrate.available.message", migrator.name)
)
