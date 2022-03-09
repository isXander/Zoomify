package dev.isxander.zoomify.utils

import net.minecraft.client.MinecraftClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val mc: MinecraftClient
    get() = MinecraftClient.getInstance()

val logger: Logger
    get() = LoggerFactory.getLogger("Zoomify")
