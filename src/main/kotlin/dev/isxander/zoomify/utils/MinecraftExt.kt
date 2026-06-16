package dev.isxander.zoomify.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

val minecraft: Minecraft = Minecraft.getInstance()

fun toast(
    title: Component,
    description: Component,
    longer: Boolean = false
) {
    val toastId = if (longer) SystemToast.SystemToastId.UNSECURE_SERVER_WARNING else SystemToast.SystemToastId.PERIODIC_NOTIFICATION

    //? if >=26.2 {
    SystemToast.add(
        minecraft.gui.toastManager(),
        toastId,
        title,
        description
    )
    //?} else {
    /*minecraft.toastManager.addToast(
        SystemToast.multiline(
            minecraft,
            toastId,
            title,
            description
        )
    )
    *///?}
}

fun zoomifyRl(path: String) =
    Identifier.fromNamespaceAndPath("zoomify", path)

//? if >=26.2 {
fun Minecraft.setScreen(screen: Screen?) =
    gui.setScreen(screen)

val Minecraft.screen: Screen?
    get() = gui.screen()
//?}
