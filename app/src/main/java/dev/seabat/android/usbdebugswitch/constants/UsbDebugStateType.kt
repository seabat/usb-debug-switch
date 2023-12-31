package dev.seabat.android.usbdebugswitch.constants

enum class UsbDebugStateType(val key: String) {
    ON("on"),
    OFF("off");
    companion object {
        fun fromKey(key: String) = try {
            UsbDebugStateType.values().first { it.key == key }
        } catch(e: NoSuchElementException) {
            OFF
        }
    }
}