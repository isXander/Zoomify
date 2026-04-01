package dev.isxander.zoomify.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

val minecraft: Minecraft = Minecraft.getInstance()

fun toast(
    title: Component,
    description: Component,
    longer: Boolean = false
): SystemToast = SystemToast.multiline(
    minecraft,
    if (longer) SystemToast.SystemToastId.UNSECURE_SERVER_WARNING else SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
    title,
    description
).also {
    minecraft.toastManager.addToast(it)
}

fun zoomifyRl(path: String) =
    Identifier.fromNamespaceAndPath("zoomify", path)
