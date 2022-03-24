package dev.isxander.zoomify.api.metrics

import com.mojang.authlib.minecraft.UserApiService
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.mixins.AccessorMinecraftClient
import dev.isxander.zoomify.utils.logger
import dev.isxander.zoomify.utils.mc
import net.fabricmc.loader.api.FabricLoader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

object UniqueUsersMetric {
    fun url(uuid: String) =
        URI("https://api.isxander.dev/metric/put/zoomify?type=users&uuid=$uuid")

    fun putApi() {
        if ((mc as AccessorMinecraftClient).userApiService == UserApiService.OFFLINE) {
            if (!FabricLoader.getInstance().isDevelopmentEnvironment)
                logger.warn("Looks like you have cracked minecraft or have no internet connection!")
            return
        }

        try {
            val client = HttpClient.newHttpClient()
            val request = HttpRequest.newBuilder(url(mc.session.profile.id.toString())).apply {
                header("User-Agent", "Zoomify/${Zoomify.VERSION}")
                header("accept", "application/json")
            }.build()

            val response = client.send(request, BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                logger.error("Failed to put unique users metric: ${response.body()}")
            } else {
                logger.info("Successfully put unique users metric")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
