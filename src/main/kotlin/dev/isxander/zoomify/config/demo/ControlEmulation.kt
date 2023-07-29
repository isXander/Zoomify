package dev.isxander.zoomify.config.demo

fun interface ControlEmulation {
    fun tick(imageRenderer: ZoomDemoImageRenderer)

    fun pause(imageRenderer: ZoomDemoImageRenderer) {}

    fun setup(imageRenderer: ZoomDemoImageRenderer) {}

    object InitialOnly : ControlEmulation {
        private var switchPauseTicks = 0

        override fun tick(imageRenderer: ZoomDemoImageRenderer) {
            // if the previous state is identical to the current state, pause for a bit, then inverse what we just did
            if (switchPauseTicks > 0) {
                switchPauseTicks--
                if (switchPauseTicks == 0) {
                    imageRenderer.keyDown = !imageRenderer.keyDown
                }
            } else if (imageRenderer.zoomHelper.getZoomDivisor(1f) == imageRenderer.zoomHelper.getZoomDivisor(0f)) {
                switchPauseTicks = 20
            }
        }

        override fun pause(imageRenderer: ZoomDemoImageRenderer) {
            imageRenderer.keyDown = false
            switchPauseTicks = 20
        }

        override fun setup(imageRenderer: ZoomDemoImageRenderer) {
            imageRenderer.keyDown = true
        }
    }

    object ScrollOnly : ControlEmulation {
        private var scrollPauseTicks = 0
        private var reverse = false

        override fun tick(imageRenderer: ZoomDemoImageRenderer) {
            if (imageRenderer.zoomHelper.maxScrollTiers() == 0) {
                imageRenderer.zoomHelper.setToZero(initial = false, scroll = true)
                imageRenderer.scrollTiers = 0
                return
            }

            if (scrollPauseTicks > 0) {
                scrollPauseTicks--
            } else {
                scrollPauseTicks = 3

                if (!reverse) {
                    imageRenderer.scrollTiers++
                    if (imageRenderer.scrollTiers >= imageRenderer.zoomHelper.maxScrollTiers()) {
                        reverse = true
                        scrollPauseTicks = 20
                    }
                } else {
                    imageRenderer.scrollTiers--
                    if (imageRenderer.scrollTiers <= 0) {
                        reverse = false
                        scrollPauseTicks = 20
                    }
                }
            }
        }


        override fun setup(imageRenderer: ZoomDemoImageRenderer) {
            imageRenderer.keyDown = true
            imageRenderer.zoomHelper.skipInitial()
        }

        override fun pause(imageRenderer: ZoomDemoImageRenderer) {
            scrollPauseTicks = 20
            reverse = false
            imageRenderer.scrollTiers = 0
            imageRenderer.zoomHelper.skipInitial()
        }
    }
}
