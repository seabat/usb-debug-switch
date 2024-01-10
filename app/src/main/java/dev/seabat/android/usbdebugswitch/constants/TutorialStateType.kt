package dev.seabat.android.usbdebugswitch.constants

enum class TutorialStateType(val key: String) {
    DISPLAYED("displayed"),
    NOT_DISPLAYED("not_displayed");
    companion object {
        fun fromKey(key: String) = try {
            TutorialStateType.values().first { it.key == key }
        } catch(e: NoSuchElementException) {
            NOT_DISPLAYED
        }
    }
}