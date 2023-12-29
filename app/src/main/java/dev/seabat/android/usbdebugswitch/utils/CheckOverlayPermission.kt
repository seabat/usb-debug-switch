package dev.seabat.android.usbdebugswitch.utils

import android.content.Context
import android.os.Build
import android.provider.Settings

class CheckOverlayPermission(val context: Context) {
    operator fun invoke(enabled: () -> Unit, disabled: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            enabled()
            return
        }

        if (Settings.canDrawOverlays(context)) {
            enabled()
        } else {
            disabled()
        }
    }
}