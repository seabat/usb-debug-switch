package dev.seabat.android.usbdebugswitch.utils

import android.content.Context
import android.os.Build
import android.provider.Settings

object OverlayPermissionChecker {

    fun isEnabled(contex: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        //AndroidManifest.xml に android.permission.SYSTEM_ALERT_WINDOW を追加する必要あり

        if (Settings.canDrawOverlays(contex)) {
            return true
        }

        return false
    }
}