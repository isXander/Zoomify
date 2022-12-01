package dev.isxander.zoomify.config.migrator

import dev.isxander.zoomify.config.migrator.impl.OkZoomerMigrator
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

interface Migrator {
    val name: Text

    fun isMigrationAvailable(): Boolean

    fun migrate(migration: Migration)

    companion object {
        val MIGRATORS = listOf<Migrator>(OkZoomerMigrator)

        fun checkMigrations() {
            val available = MIGRATORS.filter { it.isMigrationAvailable() }

            if (available.isEmpty())
                return

            var lastScreen = MinecraftClient.getInstance().currentScreen

            for (migrator in available) {
                lastScreen = MigrationAvailableScreen(migrator, lastScreen)
            }

            MinecraftClient.getInstance().setScreen(lastScreen)
        }
    }
}
