package dev.isxander.zoomify.config.migrator

import net.minecraft.text.Text
import net.minecraft.util.Formatting

class Migration {
    private val warnings = arrayListOf<Text>()
    private val errors = arrayListOf<Text>()
    var requireRestart = false
        private set

    fun error(text: Text) = errors.add(text)
    fun warn(text: Text) = warnings.add(text)

    fun requireRestart() {
        requireRestart = true
    }

    fun generateReport(): Text {
        val text = Text.empty()

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
