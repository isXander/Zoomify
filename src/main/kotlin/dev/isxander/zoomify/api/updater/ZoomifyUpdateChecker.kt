package dev.isxander.zoomify.api.updater

import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.api.metrics.UniqueUsersMetric
import dev.isxander.zoomify.utils.logger
import dev.isxander.zoomify.utils.mc
import net.minecraft.SharedConstants
import org.bundleproject.libversion.Version
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URI

object ZoomifyUpdateChecker {
    private val url = URI("https://api.isxander.dev/updater/latest/zoomify?loader=fabric&minecraft=${SharedConstants.getGameVersion().name}")

    fun getLatestVersion(): Version? {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder(url).apply {
            header("User-Agent", "Zoomify/${Zoomify.VERSION}")
            header("accept", "application/json")
        }.build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val json = Json.decodeFromString<VersionResponse>(response.body())

        if (!json.success)
            logger.error("Failed to get latest version from $url: ${json.error}")

        return json.version?.also { logger.info("Latest version is $it") }
    }

    @Serializable
    data class VersionResponse(val success: Boolean, val version: Version? = null, val error: String? = null)
}
