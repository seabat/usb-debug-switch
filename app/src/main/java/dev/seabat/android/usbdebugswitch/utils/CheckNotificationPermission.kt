package dev.seabat.android.usbdebugswitch.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

class CheckNotificationPermission(val context: Context) {
    operator fun invoke(enabled: () -> Unit, disabled: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            enabled()
            return
        }

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