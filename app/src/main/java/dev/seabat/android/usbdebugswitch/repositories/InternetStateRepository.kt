package dev.seabat.android.usbdebugswitch.repositories

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import dev.seabat.android.usbdebugswitch.MainApplication


class InternetStateRepository(
    private val context: Context = MainApplication.instance
) {
   fun setEnabled(enable: Boolean, activity: Activity) {
        setWifiEnabled(enable, activity)
   }

    private fun setWifiEnabled(enable: Boolean, activity: Activity) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10以降
            activity.startActivity(Intent(Settings.Panel.ACTION_WIFI))
        } else {
            // Android 9以前
            @Suppress("DEPRECATION")
            wifiManager.isWifiEnabled = enable
        }
    }
}