package dev.isxander.zoomify.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.network.chat.Component

val minecraft: Minecraft
    get() = Minecraft.getInstance()

fun GuiGraphics.pushPose() = pose().pushPose()
fun GuiGraphics.popPose() = pose().popPose()
fun GuiGraphics.translate(x: Double, y: Double, z: Double) = pose().translate(x, y, z)
fun GuiGraphics.translate(x: Float, y: Float, z: Float) = pose().translate(x, y, z)
fun GuiGraphics.scale(x: Float, y: Float, z: Float) = pose().scale(x, y, z)

fun toast(title: Component, description: Component, longer: Boolean = false) {
    SystemToast.multiline(
        minecraft,
        /*? if >=1.20.4 {*//*
        if (longer) SystemToast.SystemToastId.UNSECURE_SERVER_WARNING
        else SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
        *//*?} else {*/
        if (longer) SystemToast.SystemToastIds.UNSECURE_SERVER_WARNING
        else SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
        /*?}*/
        title,
        description,
    ).also { minecraft.toasts.addToast(it) }
}
