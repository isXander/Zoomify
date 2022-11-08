package dev.isxander.zoomify.config.migrator

import net.minecraft.text.Text
import net.minecraft.util.Formatting

class Migration {
    private val warnings = mutableListOf<Text>()
    private val errors = mutableListOf<Text>()
    var requireRestart = false
        private set

    fun error(text: Text) = errors.add(text)
    fun warn(text: Text) = warnings.add(text)

    fun requireRestart() {
        requireRestart = true
    }

    fun generateReport(): Text {
        val text = Text.empty()

        val errors = this.errors
        if (requireRestart) {
            errors.add(0, Text.translatable("zoomify.migrate.restart"))
        }

        for (error in errors) {
            val line = Text.empty()
                .append("\u25C6 ")
                .append(error)
                .append("\n")
                .formatted(Formatting.RED)
            text.append(line)
        }

        for (warning in warnings) {
            val line = Text.empty()
                .append("\u25C6 ")
                .append(warning)
                .append("\n")
                .formatted(Formatting.YELLOW)
            text.append(line)
        }

        return text
    }
}
