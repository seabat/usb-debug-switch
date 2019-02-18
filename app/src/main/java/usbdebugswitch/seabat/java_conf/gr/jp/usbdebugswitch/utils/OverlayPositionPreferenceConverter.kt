package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch

import android.view.Gravity

object OverlayPositionPreferenceConverter {
    val CENTER = "CENTER"
    val LEFT = "LEFT"
    val RIGHT = "RIGHT"
    val TOP = "TOP"
    val BOTTOM = "BOTTOM"


    // methods

    fun convertPrefValueToViewValue(prefValue: String): Int {
        var gravity = 0

        if (prefValue == CENTER) {
            gravity = Gravity.CENTER
        } else if (prefValue == LEFT) {
            gravity = Gravity.LEFT
        } else if (prefValue == RIGHT) {
            gravity = Gravity.RIGHT
        } else if (prefValue == TOP) {
            gravity = Gravity.TOP
        } else if (prefValue == BOTTOM) {
            gravity = Gravity.BOTTOM
        } else {
            throw IllegalArgumentException("$prefValue is not gravity value.")
        }

        return gravity
    }
}