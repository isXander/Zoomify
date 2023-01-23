package dev.isxander.zoomify.config.migrator.impl

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import dev.isxander.zoomify.Zoomify
import dev.isxander.zoomify.config.*
import dev.isxander.zoomify.config.migrator.Migration
import dev.isxander.zoomify.config.migrator.Migrator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.Text
import java.nio.file.Files
import dev.isxander.zoomify.config.migrator.impl.OkZoomerMigrator.OkZoomerConfig.Features.CinematicCameraState
import dev.isxander.zoomify.config.migrator.impl.OkZoomerMigrator.OkZoomerConfig.Features.ZoomMode
import dev.isxander.zoomify.config.migrator.impl.OkZoomerMigrator.OkZoomerConfig.Features.Overlay
import dev.isxander.zoomify.config.migrator.impl.OkZoomerMigrator.OkZoomerConfig.Features.SpyglassDependency
import dev.isxander.zoomify.utils.TransitionType
import kotlin.math.roundToInt

object OkZoomerMigrator : Migrator {
    private val file = FabricLoader.getInstance().configDir.resolve("ok_zoomer/config.toml")
    private val toml = Toml(
        inputConfig = TomlInputConfig(
            ignoreUnknownNames = true
        )
    )

    override val name: Text = Text.translatable("zoomify.migrate.okz")

    override fun isMigrationAvailable(): Boolean =
        Files.exists(file)

    override fun migrate(migration: Migration) {
        val okz = toml.decodeFromString<OkZoomerConfig>(Files.readString(file))

        when (okz.features.cinematicCamera) {
            CinematicCameraState.VANILLA ->
                ZoomifySettings.cinematicCamera = 100
            CinematicCameraState.MULTIPLIED ->
                ZoomifySettings.cinematicCamera = (1 / okz.values.cinematicMultiplier * 100).toInt()
            CinematicCameraState.OFF ->
                ZoomifySettings.cinematicCamera = 0
        }

        if (okz.features.reduceSensitivity) {
            ZoomifySettings.relativeSensitivity = 100
        } else {
            ZoomifySettings.relativeSensitivity = 0
        }

        when (okz.features.zoomMode) {
            ZoomMode.HOLD ->
                ZoomifySettings.zoomKeyBehaviour = ZoomKeyBehaviour.HOLD
            ZoomMode.TOGGLE ->
                ZoomifySettings.zoomKeyBehaviour = ZoomKeyBehaviour.TOGGLE
            ZoomMode.PERSISTENT -> {
                migration.error(Text.translatable("zoomify.migrate.okz.persistent"))
            }
        }

        ZoomifySettings._keybindScrolling = okz.features.extraKeyBinds
        migration.requireRestart()

        when (okz.features.zoomOverlay) {
            Overlay.OFF ->
                ZoomifySettings.spyglassOverlayVisibility = OverlayVisibility.NEVER
            Overlay.SPYGLASS ->
                ZoomifySettings.spyglassOverlayVisibility = when (okz.features.spyglassDependency) {
                    SpyglassDependency.REPLACE_ZOOM -> OverlayVisibility.ALWAYS
                    SpyglassDependency.REQUIRE_ITEM, SpyglassDependency.BOTH -> OverlayVisibility.HOLDING
                    SpyglassDependency.OFF -> OverlayVisibility.ALWAYS
                }
            Overlay.VIGNETTE -> {
                migration.error(Text.translatable("zoomify.migrate.okz.vignette"))
            }
        }

        when (okz.features.spyglassDependency) {
            SpyglassDependency.OFF ->
                ZoomifySettings.spyglassBehaviour = SpyglassBehaviour.COMBINE
            SpyglassDependency.REPLACE_ZOOM ->
                ZoomifySettings.spyglassBehaviour = SpyglassBehaviour.OVERRIDE
            SpyglassDependency.REQUIRE_ITEM ->
                ZoomifySettings.spyglassBehaviour = SpyglassBehaviour.ONLY_ZOOM_WHILE_HOLDING
            SpyglassDependency.BOTH ->
                ZoomifySettings.spyglassBehaviour = SpyglassBehaviour.ONLY_ZOOM_WHILE_CARRYING // FIXME: idk what this does
        }

        ZoomifySettings.spyglassSoundBehaviour =
            if (okz.tweaks.useSpyglassSounds)
                SoundBehaviour.ALWAYS
            else
                SoundBehaviour.NEVER

        ZoomifySettings.initialZoom = okz.values.zoomDivisor.roundToInt()
        migration.warn(Text.translatable("zoomify.migrate.okz.minZoomDiv"))
        ZoomifySettings.scrollZoomAmount = ((okz.values.maxZoomDivisor - ZoomifySettings.initialZoom) / Zoomify.maxScrollTiers).roundToInt()

        migration.warn(Text.translatable("zoomify.migrate.okz.stepAmt"))

        when (okz.features.zoomTransition) {
            OkZoomerConfig.Features.TransitionMode.LINEAR -> {
                migration.error(Text.translatable("zoomify.migrate.okz.linearNotSupported"))
                ZoomifySettings.zoomInTransition = TransitionType.LINEAR
                ZoomifySettings.zoomOutTransition = TransitionType.LINEAR
            }
            OkZoomerConfig.Features.TransitionMode.SMOOTH -> {
                val targetMultiplier = 1f / ZoomifySettings.initialZoom
                var multiplier = 1f
                var ticks = 0
                while (multiplier != targetMultiplier) {
                    multiplier += (targetMultiplier - multiplier) * okz.values.smoothMultiplier.toFloat()
                    ticks++
                }
                val zoomTime = (ticks * 0.05 / 0.1).roundToInt() * 0.1
                ZoomifySettings.zoomInTime = zoomTime
                ZoomifySettings.zoomOutTime = zoomTime
                ZoomifySettings.zoomInTransition = TransitionType.EASE_IN_EXP
                ZoomifySettings.zoomOutTransition = TransitionType.EASE_IN_EXP
            }
            OkZoomerConfig.Features.TransitionMode.OFF -> {
                ZoomifySettings.zoomInTime = 0.0
                ZoomifySettings.zoomOutTime = 0.0
                ZoomifySettings.zoomInTransition = TransitionType.INSTANT
                ZoomifySettings.zoomOutTransition = TransitionType.INSTANT
            }
        }


        ZoomifySettings.retainZoomSteps = !okz.tweaks.forgetZoomDivisor

        if (okz.tweaks.unbindConflictingKey) {
            Zoomify.unbindConflicting()
        }
    }

    @Serializable
    data class OkZoomerConfig(
        val features: Features,
        val values: Values,
        val tweaks: Tweaks,
    ) {
        @Serializable
        data class Features(
            @SerialName("cinematic_camera") val cinematicCamera: CinematicCameraState,
            @SerialName("reduce_sensitivity") val reduceSensitivity: Boolean,
            @SerialName("zoom_transition") val zoomTransition: TransitionMode,
            @SerialName("zoom_mode") val zoomMode: ZoomMode,
            @SerialName("zoom_scrolling") val zoomScrolling: Boolean,
            @SerialName("extra_key_binds") val extraKeyBinds: Boolean,
            @SerialName("zoom_overlay") val zoomOverlay: Overlay,
            @SerialName("spyglass_dependency") val spyglassDependency: SpyglassDependency
        ) {
            @Serializable
            enum class CinematicCameraState {
                OFF, VANILLA, MULTIPLIED
            }

            @Serializable
            enum class TransitionMode {
                OFF, SMOOTH, LINEAR
            }

            @Serializable
            enum class ZoomMode {
                HOLD, TOGGLE, PERSISTENT
            }

            @Serializable
            enum class Overlay {
                OFF, VIGNETTE, SPYGLASS
            }

            @Serializable
            enum class SpyglassDependency {
                OFF, REQUIRE_ITEM, REPLACE_ZOOM, BOTH
            }
        }

        @Serializable
        data class Values(
            @SerialName("zoom_divisor") val zoomDivisor: Double,
            @SerialName("minimum_zoom_divisor") val minZoomDivisor: Double,
            @SerialName("maximum_zoom_divisor") val maxZoomDivisor: Double,
            @SerialName("upper_scroll_steps") val upperScrollSteps: Long,
            @SerialName("lower_scroll_steps") val lowerScrollSteps: Long,
            @SerialName("smooth_multiplier") val smoothMultiplier: Double,
            @SerialName("cinematic_multiplier") val cinematicMultiplier: Double,
            @SerialName("minimum_linear_step") val minLinearStep: Double,
            @SerialName("maximum_linear_step") val maxLinearSteps: Double,
        )

        @Serializable
        data class Tweaks(
            @SerialName("reset_zoom_with_mouse") val resetZoomWithMouse: Boolean,
            @SerialName("forget_zoom_divisor") val forgetZoomDivisor: Boolean,
            @SerialName("unbind_conflicting_key") val unbindConflictingKey: Boolean,
            @SerialName("use_spyglass_texture") val useSpyglassTexture: Boolean,
            @SerialName("use_spyglass_sounds") val useSpyglassSounds: Boolean,
        )
    }
}
