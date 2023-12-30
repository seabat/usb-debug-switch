package dev.seabat.android.usbdebugswitch.constants

enum class OverlayStateType(val key: String) {
    ON("on"),
    OFF("off");
    companion object {
        fun fromKey(key: String) = try {
            OverlayStateType.values().first { it.key == key }
        } catch(e: NoSuchElementException) {
            OFF
        }
    }
}