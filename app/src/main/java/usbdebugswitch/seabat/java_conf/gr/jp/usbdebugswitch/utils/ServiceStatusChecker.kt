package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.utils

import android.app.ActivityManager
import android.content.Context
import android.util.Log

object ServiceStatusChecker {
    // constants

    private val DEBUG = false

    val TAG = "ServiceStatusChecker"

    fun isServiceRunningInForeground(context: Context, className: String): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            // getRunningServices() は Android O で depricated になったが、自サービスに対しては
            // 引き続き有効である。
            if (DEBUG) Log.d(TAG, "isServiceRunningInForeground: " + service.service.className)
            if (service.service.className.contains(className)) {
                if (service.foreground) {
                    return true
                }

            }
        }
        return false
    }

    fun hasServiceRunning(context: Context, className: String): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            // getRunningServices() は Android O で depricated になったが、自サービスに対しては
            // 引き続き有効である。
            if (DEBUG) Log.d(TAG, "hasServiceRunning: " + service.service.className)
            if (service.service.className.contains(className)) {
                return true
            }
        }
        return false
    }
}