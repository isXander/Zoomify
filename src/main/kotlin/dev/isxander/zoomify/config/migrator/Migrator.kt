package dev.isxander.zoomify.config.migrator

import dev.isxander.zoomify.config.migrator.impl.OkZoomerMigrator
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

interface Migrator {
    val name: Component

    fun isMigrationAvailable(): Boolean

    fun migrate(migration: Migration)

    companion object {
        val MIGRATORS = listOf<Migrator>(OkZoomerMigrator)

        fun checkMigrations(): Boolean {
            val available = MIGRATORS.filter { it.isMigrationAvailable() }

            if (available.isEmpty())
                return false

            var lastScreen = Minecraft.getInstance().screen

            for (migrator in available) {
                lastScreen = MigrationAvailableScreen(migrator, lastScreen)
            }

            Minecraft.getInstance().setScreen(lastScreen)
            return true
        }
    }
}
