package dev.isxander.zoomify.config.migrator

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.AlertScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component


class MigrationResultScreen(
    migration: Migration,
    parent: Screen?,
) : AlertScreen(
    {
        if (migration.requireRestart)
            Minecraft.getInstance().stop()
        else
            Minecraft.getInstance().setScreen(parent)
    },
    Component.translatable("zoomify.migrate.result.title"),
    migration.generateReport(),
    if (migration.requireRestart)
        Component.translatable("zoomify.migrate.result.restart_game")
    else
        CommonComponents.GUI_DONE,
    true
)
