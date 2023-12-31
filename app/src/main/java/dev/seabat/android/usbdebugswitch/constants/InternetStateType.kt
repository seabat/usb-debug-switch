package dev.seabat.android.usbdebugswitch.constants

enum class InternetStateType(val key: String) {
    ON("on"),
    OFF("off");

    companion object {
        fun fromKey(key: String) = try {
            InternetStateType.values().first { it.key == key }
        } catch(e: NoSuchElementException) {
            OFF
        }
    }
}