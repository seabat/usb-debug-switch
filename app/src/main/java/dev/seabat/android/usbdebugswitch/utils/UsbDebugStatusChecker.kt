package dev.seabat.android.usbdebugswitch.utils

import android.content.Context
import android.provider.Settings

object UsbDebugStatusChecker {

    // constants

    fun isUsbDebugEnabled(context: Context): Boolean {
        // 設定から USBデバッグの状態を取得
        // NOTE: Settings.Secure.ADB_ENABLED was deprecated in API level 17
        val adb = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0)
        return adb == 1
    }
}
