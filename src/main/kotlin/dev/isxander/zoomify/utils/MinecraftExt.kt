package dev.isxander.zoomify.utils

import net.minecraft.client.gui.GuiGraphics

fun GuiGraphics.pushPose() = pose().pushPose()
fun GuiGraphics.popPose() = pose().popPose()
fun GuiGraphics.translate(x: Double, y: Double, z: Double) = pose().translate(x, y, z)
fun GuiGraphics.translate(x: Float, y: Float, z: Float) = pose().translate(x, y, z)
fun GuiGraphics.scale(x: Float, y: Float, z: Float) = pose().scale(x, y, z)
