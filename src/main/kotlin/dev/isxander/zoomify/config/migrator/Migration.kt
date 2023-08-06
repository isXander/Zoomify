package dev.isxander.zoomify.config.migrator

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

class Migration {
    private val warnings = mutableListOf<Component>()
    private val errors = mutableListOf<Component>()
    var requireRestart = false
        private set

    fun error(text: Component) = errors.add(text)
    fun warn(text: Component) = warnings.add(text)

    fun requireRestart() {
        requireRestart = true
    }

    fun generateReport(): Component {
        val text = Component.empty()

        val errors = this.errors
        if (requireRestart) {
            errors.add(0, Component.translatable("zoomify.migrate.restart"))
        }

        for (error in errors) {
            val line = Component.empty()
                .append("\u25C6 ")
                .append(error)
                .append("\n")
                .withStyle(ChatFormatting.RED)
            text.append(line)
        }

        for (warning in warnings) {
            val line = Component.empty()
                .append("\u25C6 ")
                .append(warning)
                .append("\n")
                .withStyle(ChatFormatting.YELLOW)
            text.append(line)
        }

        return text
    }
}
