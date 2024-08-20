package dev.seabat.android.usbdebugswitch.constants

/**
 * オーバーレイ操作対象 (ex. USBデバッグ)
 */
enum class SelectedOverlayType(val key: String) {
    USB_DEBUG("usb_debug"),
    INTERNET("internet");

    companion object {
        fun fromKey(key: String) = try {
            entries.first { it.key == key }
        } catch (e: NoSuchElementException) {
            USB_DEBUG
        }
    }
}