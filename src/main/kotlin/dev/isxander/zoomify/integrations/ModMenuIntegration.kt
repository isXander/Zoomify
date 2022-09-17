package dev.isxander.zoomify.integrations

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.zoomify.config.ZoomifySettings

object ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory { parent ->
        ZoomifySettings.gui(parent)
    }
}
