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
    { MinecraftClient.getInstance().setScreen(parent) },
    Text.translatable("zoomify.migrate.result.title"),
    migration.generateReport(),
    ScreenTexts.DONE,
    true
)
