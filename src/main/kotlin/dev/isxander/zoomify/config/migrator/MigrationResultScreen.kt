package dev.isxander.zoomify.config.migrator

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.NoticeScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text

class MigrationResultScreen(
    migration: Migration,
    parent: Screen?,
) : NoticeScreen(
    {
        if (migration.requireRestart)
            MinecraftClient.getInstance().scheduleStop()
        else
            MinecraftClient.getInstance().setScreen(parent)
    },
    Text.translatable("zoomify.migrate.result.title"),
    migration.generateReport(),
    if (migration.requireRestart) Text.translatable("zoomify.migrate.result.restart_game") else ScreenTexts.DONE,
    true
)
