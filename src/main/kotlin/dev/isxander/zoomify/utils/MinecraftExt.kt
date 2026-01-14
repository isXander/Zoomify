package dev.isxander.zoomify.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.network.chat.Component

val minecraft: Minecraft = Minecraft.getInstance()

fun GuiGraphics.pushPose() =
    //? if >=1.21.6 {
    pose().pushMatrix()
    //?} else {
    /*pose().pushPose()
    *///?}
fun GuiGraphics.popPose() =
    //? if >=1.21.6 {
    pose().popMatrix()
    //?} else {
    /*pose().popPose()
    *///?}
fun GuiGraphics.translate(x: Float, y: Float) = pose().translate(x, y, /*? if <1.21.6 >>*//*0.0F*/ )
fun GuiGraphics.scale(x: Float, y: Float) = pose().scale(x, y, /*? if <1.21.6 >>*//*1.0F*/ )

// i love kotlin
typealias ToastTypes =
        //? if >1.20.1 {
        SystemToast.SystemToastId
        //?} else {
        /*SystemToast.SystemToastIds
        *///?}

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
    val toastManager = /*? if >=1.21.2 {*/ minecraft.toastManager /*?} else {*/ /*minecraft.toasts *//*?}*/
    toastManager.addToast(it)
}

typealias Identifier =
    //? if >=1.21.11 {
    net.minecraft.resources.Identifier
    //?} else {
    /*net.minecraft.resources.ResourceLocation
    *///?}

fun zoomifyRl(path: String) =
    //? >=1.21 {
    Identifier.fromNamespaceAndPath("zoomify", path)
    //?} else {
    /*Identifier("zoomify", path)
    *///?}
