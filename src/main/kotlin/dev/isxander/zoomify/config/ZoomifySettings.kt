package dev.isxander.zoomify.config

import com.mojang.serialization.Codec
import dev.isxander.yacl3.config.v3.JsonFileCodecConfig
import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.config.v3.value
import dev.isxander.zoomify.utils.TransitionType
import net.fabricmc.loader.api.FabricLoader

open class ZoomifySettings() : JsonFileCodecConfig<ZoomifySettings>(
    FabricLoader.getInstance().configDir.resolve("zoomify.json")
) {
    val initialZoom by register<Int>(default = 4, Codec.INT)

    val zoomInTime by register<Double>(default = 1.0, Codec.DOUBLE)
    val zoomOutTime by register<Double>(default = 0.5, Codec.DOUBLE)

    val zoomInTransition by register<TransitionType>(default = TransitionType.EASE_OUT_EXP, TransitionType.CODEC)
    val zoomOutTransition by register<TransitionType>(default = TransitionType.EASE_OUT_EXP, TransitionType.CODEC)

    val affectHandFov by register<Boolean>(default = true, Codec.BOOL)

    val retainZoomSteps by register<Boolean>(default = false, Codec.BOOL)
    val linearLikeSteps by register<Boolean>(default = true, Codec.BOOL)

    val scrollZoom by register<Boolean>(default = true, Codec.BOOL)
    val scrollZoomAmount by register<Int>(default = 3, Codec.INT)
    val scrollZoomSmoothness by register<Int>(default = 70, Codec.INT)

    val zoomKeyBehaviour by register<ZoomKeyBehaviour>(default = ZoomKeyBehaviour.HOLD, ZoomKeyBehaviour.CODEC)

    var keybindScrolling = false
    val _keybindScrolling by register<Boolean>(default = keybindScrolling, codec = Codec.BOOL)

    val relativeSensitivity by register<Int>(default = 100, Codec.INT)
    val relativeViewBobbing by register<Boolean>(default = true, Codec.BOOL)

    val cinematicCamera by register<Int>(default = 0, Codec.INT)

    val spyglassBehaviour by register<SpyglassBehaviour>(default = SpyglassBehaviour.COMBINE, SpyglassBehaviour.CODEC)
    val spyglassOverlayVisibility by register<OverlayVisibility>(default = OverlayVisibility.HOLDING, OverlayVisibility.CODEC)
    val spyglassSoundBehaviour by register<SoundBehaviour>(default = SoundBehaviour.WITH_OVERLAY, SoundBehaviour.CODEC)

    val secondaryZoomAmount by register<Int>(default = 4, Codec.INT)
    val secondaryZoomInTime by register<Double>(default = 10.0, Codec.DOUBLE)
    val secondaryZoomOutTime by register<Double>(default = 1.0, Codec.DOUBLE)
    val secondaryHideHUDOnZoom by register<Boolean>(default = true, Codec.BOOL)

    var firstLaunch = false
    val _firstLaunch by register<Boolean>(default = true, Codec.BOOL)

    final val allSettings = arrayOf(
        initialZoom,
        zoomInTime,
        zoomOutTime,
        zoomInTransition,
        zoomOutTransition,
        affectHandFov,
        retainZoomSteps,
        linearLikeSteps,
        scrollZoom,
        scrollZoomAmount,
        scrollZoomSmoothness,
        zoomKeyBehaviour,
        relativeSensitivity,
        relativeViewBobbing,
        cinematicCamera,
        spyglassBehaviour,
        spyglassOverlayVisibility,
        spyglassSoundBehaviour,
        secondaryZoomAmount,
        secondaryZoomInTime,
        secondaryZoomOutTime,
        secondaryHideHUDOnZoom
    )

    constructor(settings: ZoomifySettings) : this() {
        repeat(allSettings.size) { i ->
            allSettings[i].value = settings.allSettings[i].value
        }

        this.initialZoom.value = settings.initialZoom.value
        this.zoomInTime.value = settings.zoomInTime.value
        this.zoomOutTime.value = settings.zoomOutTime.value
        this.zoomInTransition.value = settings.zoomInTransition.value
        this.zoomOutTransition.value = settings.zoomOutTransition.value
        this.affectHandFov.value = settings.affectHandFov.value
        this.retainZoomSteps.value = settings.retainZoomSteps.value
        this.linearLikeSteps.value = settings.linearLikeSteps.value
        this.scrollZoom.value = settings.scrollZoom.value
        this.scrollZoomAmount.value = settings.scrollZoomAmount.value

    }

    companion object : ZoomifySettings() {
        init {
            if (!loadFromFile()) {
                saveToFile()
            }

            if (_firstLaunch.value) {
                firstLaunch = true
                _firstLaunch.value = false
                saveToFile()
            }

            _keybindScrolling.value = keybindScrolling
        }
    }
}
