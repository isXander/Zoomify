package dev.isxander.zoomify.config.gui

import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import java.util.*

class ButtonListEntry(
    fieldName: Text,
    buttonName: Text,
    val action: () -> Unit,
    tooltipSupplier: () -> Optional<Array<out Text>>,
    requiresRestart: Boolean
) : TooltipListEntry<Unit>(fieldName, tooltipSupplier, requiresRestart) {
    private val buttonWidget = ButtonWidget(
        0, 0, 150, 20, buttonName
    ) { action() }
    private val widgets = listOf<ClickableWidget>(buttonWidget)

    override fun render(
        matrices: MatrixStack,
        index: Int,
        y: Int, x: Int,
        entryWidth: Int, entryHeight: Int,
        mouseX: Int, mouseY: Int,
        isHovered: Boolean,
        delta: Float
    ) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta)
        val window = MinecraftClient.getInstance().window

        buttonWidget.y = y
        val displayedFieldName = this.displayedFieldName
        if (MinecraftClient.getInstance().textRenderer.isRightToLeft) {
            MinecraftClient.getInstance().textRenderer
                .drawWithShadow(
                    matrices,
                    displayedFieldName.asOrderedText(),
                    (window.scaledWidth - x - MinecraftClient.getInstance().textRenderer.getWidth(
                        displayedFieldName
                    )).toFloat(),
                    (y + 6).toFloat(),
                    16777215
                )
            buttonWidget.x = x
        } else {
            MinecraftClient.getInstance().textRenderer
                .drawWithShadow(
                    matrices,
                    displayedFieldName.asOrderedText(),
                    x.toFloat(),
                    (y + 6).toFloat(),
                    this.preferredTextColor
                )
            buttonWidget.x = x + entryWidth - 150
        }

        buttonWidget.width = 150
        buttonWidget.render(matrices, mouseX, mouseY, delta)
    }

    override fun children() = widgets
    override fun narratables() = widgets

    override fun getValue() = Unit
    override fun getDefaultValue() = Optional.empty<Unit>()
    override fun save() {}
}
