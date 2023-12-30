package dev.seabat.android.usbdebugswitch.constants

enum class SelectedOverlayType(val key: String) {
    USB_DEBUG("usb_debug"),
    INTERNET("internet");

    companion object {
        fun fromKey(key: String) = try {
            SelectedOverlayType.values().first { it.key == key }
        } catch(e: NoSuchElementException) {
            USB_DEBUG
        }
    }
}