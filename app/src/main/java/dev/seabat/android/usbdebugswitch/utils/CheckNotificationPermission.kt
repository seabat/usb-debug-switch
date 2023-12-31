package dev.seabat.android.usbdebugswitch.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class CheckNotificationPermission(val context: Context) {
    operator fun invoke(enabled: () -> Unit, disabled: () -> Unit) {
        ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ).also { checkPermissionResult ->
            if (checkPermissionResult == PackageManager.PERMISSION_GRANTED) {
                enabled()
            } else {
                disabled()
            }
        }
    }
}