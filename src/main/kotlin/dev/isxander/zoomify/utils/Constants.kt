package dev.isxander.zoomify.utils

import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

val mc: MinecraftClient
    get() = MinecraftClient.getInstance()

val logger: Logger
    get() = LogManager.getLogger("Zoomify")
