package dev.isxander.zoomify.config.gui

import me.shedaniel.clothconfig2.impl.builders.FieldBuilder
import net.minecraft.text.Text
import java.util.*

class ButtonEntryBuilder(fieldNameKey: Text, private val buttonNameKey: Text, private val action: () -> Unit) :
    FieldBuilder<Unit, ButtonListEntry>(Text.empty(), fieldNameKey) {
    private var tooltipSupplier: () -> Optional<Array<out Text>> = { Optional.empty() }

    fun requireRestart(): ButtonEntryBuilder {
        requireRestart(true)
        return this
    }

    fun setTooltipSupplier(supplier: () -> Optional<Array<out Text>>): ButtonEntryBuilder {
        tooltipSupplier = supplier
        return this
    }

    fun setTooltip(tooltips: Optional<Array<out Text>>): ButtonEntryBuilder {
        tooltipSupplier = { tooltips }
        return this
    }

    fun setTooltip(vararg tooltips: Text): ButtonEntryBuilder {
        tooltipSupplier = { Optional.of(tooltips) }
        return this
    }

    override fun build(): ButtonListEntry {
        return ButtonListEntry(fieldNameKey, buttonNameKey, action, tooltipSupplier, isRequireRestart)
    }
}
