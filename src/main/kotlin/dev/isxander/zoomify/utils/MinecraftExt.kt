package dev.isxander.zoomify.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

val minecraft: Minecraft = Minecraft.getInstance()

fun GuiGraphics.pushPose() = pose().pushPose()
fun GuiGraphics.popPose() = pose().popPose()
fun GuiGraphics.translate(x: Double, y: Double, z: Double) = pose().translate(x, y, z)
fun GuiGraphics.translate(x: Float, y: Float, z: Float) = pose().translate(x, y, z)
fun GuiGraphics.scale(x: Float, y: Float, z: Float) = pose().scale(x, y, z)

// i love kotlin
typealias ToastTypes =
        /*? if >1.20.1 {*/
        SystemToast.SystemToastId
        /*?} else {*//*
        SystemToast.SystemToastIds
        *//*?}*/

fun toast(
    title: Component,
    description: Component,
    longer: Boolean = false
): SystemToast = SystemToast.multiline(
    minecraft,
    if (longer) ToastTypes.UNSECURE_SERVER_WARNING else ToastTypes.PERIODIC_NOTIFICATION,
    title,
    description
).also {
    minecraft.toasts.addToast(it)
}

fun zoomifyRl(path: String) =
    //? if >=1.21 {
    /*ResourceLocation.fromNamespaceAndPath("zoomify", path)
    *///?} else {
    ResourceLocation("zoomify", path)
    //?}
